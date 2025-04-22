package br.com.simulator.credit.creditas.messaging.sqs.simulation

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toLoanSimulationCommandDto
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationResponseDto
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration
import com.trendyol.kediatr.Mediator
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.MANUAL
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable
class BulkSimulationListener(
  private val repository: BulkSimulationPersistenceAdapter,
  private val mediator: Mediator,
  private val policyConfiguration: PolicyConfiguration
) {
  private val logger = LoggerFactory.getLogger(BulkSimulationListener::class.java)

  @OptIn(DelicateCoroutinesApi::class)
  @SqsListener(
    "\${cloud.aws.sqs.queues.bulkSimulationQueue}",
    acknowledgementMode = MANUAL
  )
  fun onMessage(
    message: BulkSimulationMessage,
    acknowledgment: Acknowledgement
  ) {
    GlobalScope.launch {
      try {
        processMessage(message)
        acknowledgment.acknowledge()
        logger.info("Message acknowledged (bulkId=${message.bulkId})")
      } catch (ex: Exception) {
        logger.error(
          "Processing failed for bulkId=${message.bulkId}, message will remain in the queue: ${ex.message}",
          ex
        )
      }
    }
  }

  private suspend fun processMessage(message: BulkSimulationMessage) {
    logger.info("Processing bulk simulation: $message")
    val processedCount = AtomicInteger(0)
    val total = message.simulations.size

    message.simulations.forEach { sim ->
      val request = sim.toLoanSimulationCommandDto(sim.policyType)
      try {
        val policy = policyConfiguration.resolve(request.policyType)
        val result = mediator.send(request.toSimulateLoanCommand(policy))

        val count = processedCount.incrementAndGet()
        val isLast = count >= total
        val dto = BulkSimulationResponseDto(
          source = BulkSimulationResponseDto.Source(amount = request.loanAmount),
          target = BulkSimulationResponseDto.Target(
            convertedAmount = result.target.convertedAmount,
            totalPayment = result.target.totalPayment,
            monthlyInstallment = result.target.monthlyInstallment,
            totalInterest = result.target.totalInterest,
            annualInterestRate = result.target.annualInterestRate
          )
        )

        repository.updateIncrementAndPushResult(message.bulkId, dto, isLast)
        logger.info("Processed simulation #${count}/$total for bulkId=${message.bulkId}")
      } catch (ex: Exception) {
        logger.error("Error processing simulation for bulkId=${message.bulkId}: ${ex.message}", ex)
        repository.updateStatus(message.bulkId, BulkSimulationStatus.FAILED)
      }
    }
  }
}
