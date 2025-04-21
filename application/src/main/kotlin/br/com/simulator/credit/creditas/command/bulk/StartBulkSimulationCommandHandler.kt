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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

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

    sendInBatches(command.bulkId, simulationMessages, INITIAL_BATCH_SIZE)
  }

  private fun sendInBatches(
    bulkId: UUID,
    simulations: List<BulkSimulationMessage.LoanSimulationMessage>,
    batchSize: Int,
  ) {
    if (batchSize < 1) {
      logger.error("Batch size too small, cannot send simulations")
      throw IllegalStateException("Cannot send simulations, individual messages exceed SQS limit")
    }

    simulations.chunked(batchSize).forEachIndexed { index, batch ->
      val message =
        BulkSimulationMessage(
          bulkId = bulkId,
          simulations = batch,
        )

      val messageJson = objectMapper.writeValueAsString(message)

      if (messageJson.length > SQS_MESSAGE_SIZE_LIMIT) {
        logger.info("Batch size (${messageJson.length} bytes) exceeds limit, reducing size and retrying")
        sendInBatches(bulkId, batch, batchSize / 2)
      } else {
        publisher.send(message)
        logger.info("Sent batch ${index + 1} with ${batch.size} simulations (${messageJson.length} bytes)")
      }
    }
  }

  companion object {
    private const val INITIAL_BATCH_SIZE = 100
    private const val SQS_MESSAGE_SIZE_LIMIT = 250000
  }
}
