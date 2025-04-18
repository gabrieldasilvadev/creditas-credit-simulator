package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.config.BulkSimulationConfig
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationResponseDto
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.Mediator
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable("StartBulkSimulationCommandHandler")
class StartBulkSimulationCommandHandler(
  private val repository: BulkSimulationPersistenceAdapter,
  private val mediator: Mediator,
  private val config: BulkSimulationConfig,
) : CommandHandler<StartBulkSimulationCommand> {
  private val mutex = Mutex()
  private val logger: Logger = LoggerFactory.getLogger(StartBulkSimulationCommandHandler::class.java)

  override suspend fun handle(command: StartBulkSimulationCommand): Unit =
    coroutineScope {
      logger.info("Starting bulk simulation: $command")
      val document = initializeOrGetDocument(command)
      val processedCount = AtomicInteger(document.processed)
      val totalToProcess = command.simulations.size

      try {
        command.simulations
          .chunked(config.size)
          .asFlow()
          .buffer(config.buffer)
          .map { batch ->
            async(Dispatchers.IO) {
              processBatch(
                batch = batch,
                document = document,
                processedCount = processedCount,
                total = totalToProcess
              )
            }
          }
          .collect { it.await() }

        updateStatus(command.bulkId, BulkSimulationStatus.COMPLETED)
      } catch (ex: Exception) {
        logger.error("Error during batch processing: ${ex.message}", ex)
        updateStatus(command.bulkId, BulkSimulationStatus.FAILED)
      }
    }

  private suspend fun initializeOrGetDocument(command: StartBulkSimulationCommand) =
    mutex.withLock {
      repository.findById(command.bulkId).orElseGet {
        repository.save(
          BulkSimulationDocument(
            id = command.bulkId,
            status = BulkSimulationStatus.PROCESSING,
            processed = 0,
            total = command.simulations.size,
          ),
        )
      }.also {
        logger.info("Initialized bulk simulation document: $it")
      }
    }

  private suspend fun processBatch(
    batch: List<LoanSimulationCommandDto>,
    document: BulkSimulationDocument,
    processedCount: AtomicInteger,
    total: Int,
  ) = coroutineScope {
    batch.map { request -> async(Dispatchers.IO) { processSimulation(request, document, processedCount, total) } }
      .awaitAll()
  }

  private suspend fun processSimulation(
    request: LoanSimulationCommandDto,
    document: BulkSimulationDocument,
    processedCount: AtomicInteger,
    total: Int,
  ) {
    try {
      val result = mediator.send(request.toSimulateLoanCommand())
      mutex.withLock {
        val updatedDocument = repository.findById(document.id).orElse(document)
        repository.save(
          updatedDocument.copy(
            processed = processedCount.incrementAndGet(),
            status = determineStatus(processedCount.get(), total),
            results = updatedDocument.results + result.toResponseDto(request),
          ).also {
            logger.info("Processed simulation - Request: $request | Result: $result")
          }
        )
      }
    } catch (ex: Exception) {
      logger.error("Error processing simulation: ${ex.message}", ex)
      updateStatus(document.id, BulkSimulationStatus.FAILED)
    }
  }

  private fun determineStatus(
    processed: Int,
    total: Int,
  ) = if (processed >= total) BulkSimulationStatus.COMPLETED else BulkSimulationStatus.PROCESSING

  private suspend fun updateStatus(
    bulkId: UUID,
    status: BulkSimulationStatus,
  ) = mutex.withLock {
    repository.findById(bulkId).let { repository.save(it.get().copy(status = status)) }.also {
      logger.info("Updated bulk simulation document status: $it")
    }
  }

  private fun LoanSimulationHttpResponse.toResponseDto(request: LoanSimulationCommandDto) =
    BulkSimulationResponseDto(
      source = BulkSimulationResponseDto.Source(request.loanAmount),
      target =
        BulkSimulationResponseDto.Target(
          convertedAmount = target.convertedAmount,
          totalPayment = target.totalPayment,
          monthlyInstallment = target.monthlyInstallment,
          totalInterest = target.totalInterest,
          annualInterestRate = target.annualInterestRate,
        ),
    ).also {
      logger.info("Converted simulation response to DTO: $it")
    }
}
