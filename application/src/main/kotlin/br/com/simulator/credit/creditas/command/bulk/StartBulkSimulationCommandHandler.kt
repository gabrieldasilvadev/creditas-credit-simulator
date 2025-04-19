package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import com.trendyol.kediatr.CommandHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable("StartBulkSimulationCommandHandler")
class StartBulkSimulationCommandHandler(
  private val repository: BulkSimulationPersistenceAdapter,
  private val publisher: SqsBulkSimulationQueuePublisherAdapter,
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

    val simulations =
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
      }.toList()

    publisher.send(
      BulkSimulationMessage(
        bulkId = command.bulkId,
        simulations = simulations,
      ).also {
        logger.info("Bulk simulation message created: $it")
      }
    )
  }
}
