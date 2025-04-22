package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.trendyol.kediatr.CommandHandler
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable
class StartBulkSimulationCommandHandler(
  private val repository: BulkSimulationPersistenceAdapter,
  private val publisher: SqsBulkSimulationQueuePublisherAdapter,
  private val objectMapper: ObjectMapper,
) : CommandHandler<StartBulkSimulationCommand> {
  private val logger: Logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun handle(command: StartBulkSimulationCommand) {
    logger.info("Scheduling bulk simulation for async processing: $command")

    repository.save(
      BulkSimulationDocument(
        id = command.bulkId,
        status = BulkSimulationStatus.PROCESSING,
        processed = 0,
        total = command.simulations.size,
      ),
    ).also {
      logger.info("Initialized bulk simulation document: $it")
    }

    val simulationMessages =
      command.simulations.map { simulationData ->
        BulkSimulationMessage.LoanSimulationMessage(
          loanAmount = simulationData.loanAmount,
          customerInfo = simulationData.customerInfo,
          months = simulationData.months,
          interestRate = simulationData.interestRate.amount.toMoney(),
          sourceCurrency = simulationData.sourceCurrency,
          targetCurrency = simulationData.targetCurrency,
          policyType = simulationData.policyType,
        )
      }

    sendInBatches(command.bulkId, simulationMessages)
  }

  private fun sendInBatches(
    bulkId: UUID,
    simulations: List<BulkSimulationMessage.LoanSimulationMessage>
  ) {
    val queue = ArrayDeque(listOf(simulations))
    var batchNumber = 0

    while (queue.isNotEmpty()) {
      val batch = queue.removeFirst()
      val message = BulkSimulationMessage(bulkId, batch)
      val payload = objectMapper.writeValueAsBytes(message)
      val size = payload.size

      if (size <= SQS_MESSAGE_SIZE_LIMIT) {
        publisher.send(message)
        logger.info("Sent batch ${++batchNumber} with ${batch.size} items (${size} bytes)")
      } else if (batch.size > 1) {
        val mid = batch.size / 2
        logger.warn(
          "Payload $size bytes too big for ${batch.size} items; splitting into " +
            "$mid and ${batch.size - mid}"
        )
        queue.addFirst(batch.subList(mid, batch.size))
        queue.addFirst(batch.subList(0, mid))
      } else {
        logger.error("Single simulation payload $size bytes exceeds SQS limit")
        throw IllegalStateException("Cannot send single simulation; payload too large")
      }
    }
  }

  companion object {
    private const val SQS_MESSAGE_SIZE_LIMIT = 256 * 1024
  }
}
