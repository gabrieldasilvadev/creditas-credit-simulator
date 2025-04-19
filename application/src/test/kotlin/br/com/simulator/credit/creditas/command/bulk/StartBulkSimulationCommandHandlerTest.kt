package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class StartBulkSimulationCommandHandlerTest {
  private val repository = mockk<BulkSimulationPersistenceAdapter>()
  private val publisher = mockk<SqsBulkSimulationQueuePublisherAdapter>(relaxed = true)

  private lateinit var handler: StartBulkSimulationCommandHandler

  @BeforeEach
  fun setup() {
    handler = StartBulkSimulationCommandHandler(repository, publisher)
  }

  @Test
  fun `should persist document and publish simulation message`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")

      val dto =
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 12,
          interestRate = Money(BigDecimal("0.03")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = PolicyType.AGE_BASED,
        )

      val command =
        StartBulkSimulationCommand(
          bulkId = bulkId,
          simulations = listOf(dto),
        )

      every {
        repository.save(
          match {
            it.id == bulkId &&
              it.status == BulkSimulationStatus.PROCESSING &&
              it.processed == 0 &&
              it.total == 1
          },
        )
      } returns
        BulkSimulationDocument(
          id = bulkId,
          status = BulkSimulationStatus.PROCESSING,
          processed = 0,
          total = 1,
        )

      handler.handle(command)

      verify(exactly = 1) {
        repository.save(any())
      }

      verify(exactly = 1) {
        publisher.send(
          match {
            it.bulkId == bulkId &&
              it.simulations.size == 1 &&
              it.simulations[0].loanAmount.amount == BigDecimal("10000.00") &&
              it.simulations[0].months == 12 &&
              it.simulations[0].sourceCurrency == "BRL" &&
              it.simulations[0].targetCurrency == "USD" &&
              it.simulations[0].policyType == PolicyType.AGE_BASED
          },
        )
      }
    }
}
