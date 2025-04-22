package br.com.simulator.credit.creditas.messaging.sqs.simulation

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toLoanSimulationCommandDto
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationResponseDto
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.property.BulkSimulationProperties
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration
import com.trendyol.kediatr.Mediator
import io.awspring.cloud.sqs.annotation.SqsListener
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable
class BulkSimulationListener(
  private val repository: BulkSimulationPersistenceAdapter,
  private val mediator: Mediator,
  private val policyConfiguration: PolicyConfiguration,
  private val bulkSimulationProperties: BulkSimulationProperties,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @SqsListener("\${cloud.aws.sqs.queues.bulkSimulationQueue}")
  fun onMessage(message: BulkSimulationMessage) =
    runBlocking {
      processMessage(message)
    }

  private suspend fun processMessage(message: BulkSimulationMessage) =
    coroutineScope {
      logger.info("Processing bulk simulation message: $message")
      val processedCount = AtomicInteger(0)
      val total = message.simulations.size

      message.simulations
        .chunked(bulkSimulationProperties.size)
        .asFlow()
        .buffer(bulkSimulationProperties.buffer)
        .collect { chunk ->
          chunk.map { simulation ->
            async(Dispatchers.IO) {
              process(
                simulation.toLoanSimulationCommandDto(simulation.policyType),
                message.bulkId,
                processedCount,
                total,
              )
            }
          }.awaitAll()
        }
    }

  private suspend fun process(
    request: LoanSimulationCommandDto,
    bulkId: UUID,
    processedCount: AtomicInteger,
    total: Int,
  ) {
    val document = repository.findById(bulkId).orElseThrow()

    try {
      val interestRatePolicy = policyConfiguration.resolve(request.policyType)
      val result = mediator.send(request.toSimulateLoanCommand(interestRatePolicy))

      repository.save(
        document.copy(
          processed = processedCount.incrementAndGet(),
          status =
            if (processedCount.get() >= total) {
              BulkSimulationStatus.COMPLETED
            } else {
              BulkSimulationStatus.PROCESSING
            },
          results =
            document.results +
              listOf(
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
                ),
              ),
        ),
      )

      logger.info("Processed simulation: $request")
    } catch (ex: Exception) {
      logger.error("Error processing simulation from queue: ${ex.message}", ex)
      repository.save(document.copy(status = BulkSimulationStatus.FAILED))
    }
  }
}
