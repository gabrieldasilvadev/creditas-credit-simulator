package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationResponseDto
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import com.trendyol.kediatr.CommandHandler
import com.trendyol.kediatr.Mediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Component
@Monitorable("StartBulkSimulationCommandHandler")
class StartBulkSimulationCommandHandler(
  private val repository: BulkSimulationPersistenceAdapter,
  private val mediator: Mediator,
  @Value("\${internal.simulation.bulk.size}")
  private val bulkSize: Int,
  @Value("\${internal.simulation.bulk.buffer}")
  private val bulkBufferCapacity: Int,
) : CommandHandler<StartBulkSimulationCommand> {
  private val logger = LoggerFactory.getLogger(this::class.java)
  private val mutex = Mutex()

  override suspend fun handle(command: StartBulkSimulationCommand): Unit =
    coroutineScope {
      val currentDocument = initializeOrGetBulkDocument(command)
      val processedCount = AtomicInteger(currentDocument.processed)
      val totalToProcess = command.simulations.size

      try {
        command.simulations
          .chunked(bulkSize)
          .asFlow()
          .buffer(capacity = bulkBufferCapacity)
          .map { batch ->
            async(Dispatchers.IO) {
              processBatch(batch, currentDocument, processedCount, totalToProcess)
            }
          }
          .buffer(capacity = bulkBufferCapacity)
          .collect { it.await() }

        updateFinalStatus(command.bulkId, BulkSimulationStatus.COMPLETED)
      } catch (ex: Exception) {
        logger.error("Error during batch processing: ${ex.message}", ex)
        updateFinalStatus(command.bulkId, BulkSimulationStatus.FAILED)
      }
    }

  private suspend fun initializeOrGetBulkDocument(command: StartBulkSimulationCommand): BulkSimulationDocument =
    mutex.withLock {
      return repository.findById(command.bulkId).orElseGet {
        val newDocument =
          BulkSimulationDocument(
            id = command.bulkId,
            status = BulkSimulationStatus.PROCESSING,
            processed = 0,
            total = command.simulations.size,
          )
        repository.save(newDocument)
      }
    }

  private suspend fun processBatch(
    batch: List<LoanSimulationCommandDto>,
    currentDocument: BulkSimulationDocument,
    processedCount: AtomicInteger,
    totalToProcess: Int,
  ) = coroutineScope {
    batch.map { request ->
      async(Dispatchers.IO) {
        processSimulation(request, currentDocument, processedCount, totalToProcess)
      }
    }.awaitAll()
  }

  private suspend fun processSimulation(
    request: LoanSimulationCommandDto,
    currentDocument: BulkSimulationDocument,
    processedCount: AtomicInteger,
    totalToProcess: Int,
  ) {
    try {
      val result: LoanSimulationHttpResponse = mediator.send(request.toSimulateLoanCommand())

      mutex.withLock {
        val latestDocument = repository.findById(currentDocument.id).orElse(currentDocument)

        val processed = processedCount.incrementAndGet()
        val status = determineStatus(processed, totalToProcess)

        val updatedResults = updateResults(latestDocument, request, result)

        repository.save(
          latestDocument.copy(
            processed = processed,
            status = status,
            results = updatedResults,
          ),
        )
      }
    } catch (ex: Exception) {
      logger.error("Error when processing simulation: ${ex.message}", ex)
      mutex.withLock {
        val latestDocument = repository.findById(currentDocument.id).orElse(currentDocument)
        repository.save(latestDocument.copy(status = BulkSimulationStatus.FAILED))
      }
    }
  }

  private fun determineStatus(
    processed: Int,
    total: Int,
  ): BulkSimulationStatus =
    when {
      processed >= total -> BulkSimulationStatus.COMPLETED
      else -> BulkSimulationStatus.PROCESSING
    }

  private fun updateResults(
    document: BulkSimulationDocument,
    request: LoanSimulationCommandDto,
    result: LoanSimulationHttpResponse,
  ): List<BulkSimulationResponseDto> {
    val existingResults = document.results.toMutableList()

    val newResponse =
      BulkSimulationResponseDto(
        source = BulkSimulationResponseDto.Source(amount = request.loanAmount),
        target =
          BulkSimulationResponseDto.Target(
            convertedAmount = result.target.convertedAmount,
            totalPayment = result.target.totalPayment,
            monthlyInstallment = result.target.monthlyInstallment,
            totalInterest = result.target.totalInterest,
            annualInterestRate = result.target.annualInterestRate,
          ),
      )

    existingResults.add(newResponse)
    return existingResults
  }

  private suspend fun updateFinalStatus(
    bulkId: UUID,
    status: BulkSimulationStatus,
  ) = mutex.withLock {
    val document = repository.findById(bulkId)
    document.ifPresent { doc -> repository.save(doc.copy(status = status)) }
  }
}
