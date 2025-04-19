package br.com.simulator.credit.creditas.messaging.sqs.simulation

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toLoanSimulationCommandDto
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.command.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.property.BulkSimulationProperties
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import com.trendyol.kediatr.Mediator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

internal class BulkSimulationListenerTest {
  private val repository = mockk<BulkSimulationPersistenceAdapter>(relaxed = true)
  private val mediator = mockk<Mediator>(relaxed = true)
  private val policyConfiguration = mockk<br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration>()
  private val bulkSimulationProperties = BulkSimulationProperties(size = 10, buffer = 10)

  private lateinit var listener: BulkSimulationListener

  @BeforeEach
  fun setup() {
    listener = BulkSimulationListener(
      repository,
      mediator,
      policyConfiguration,
      bulkSimulationProperties
    )
  }

  @Test
  fun `should process simulations and save result`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val policyType = PolicyType.AGE_BASED

      val simulationMsg =
        BulkSimulationMessage.LoanSimulationMessage(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          customerInfo =
            CustomerInfo(
              customerEmail = "cliente1@teste.com",
              birthDate = LocalDate.of(1990, 1, 1),
            ),
          months = 12,
          interestRate = Money(BigDecimal("0.03")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = policyType,
        )

      val message =
        BulkSimulationMessage(
          bulkId = bulkId,
          simulations = listOf(simulationMsg),
        )

      val document =
        BulkSimulationDocument(
          id = bulkId,
          processed = 0,
          results = listOf(),
          status = BulkSimulationStatus.PROCESSING,
          total = 0,
        )

      val interestRatePolicy = mockk<InterestRatePolicy>()
      val command = simulationMsg.toLoanSimulationCommandDto(policyType).toSimulateLoanCommand(interestRatePolicy)

      val response =
        LoanSimulationHttpResponse(
          source =
            LoanSimulationHttpResponse.Source(
              amount = simulationMsg.loanAmount,
            ),
          target =
            LoanSimulationHttpResponse.Target(
              convertedAmount = Money(BigDecimal("10000.00"), Currency.USD),
              totalPayment = Money(BigDecimal("12000.00"), Currency.USD),
              monthlyInstallment = Money(BigDecimal("1000.00"), Currency.USD),
              totalInterest = Money(BigDecimal("2000.00"), Currency.USD),
              annualInterestRate = Money(BigDecimal("0.03"), Currency.USD),
            ),
        )

      every { repository.findById(bulkId) } returns Optional.of(document)
      every { policyConfiguration.resolve(policyType) } returns interestRatePolicy
      coEvery { mediator.send(command) } returns response

      listener.onMessage(message)

      verify(exactly = 1) {
        repository.save(
          match {
            it.processed == 1 &&
              it.status == BulkSimulationStatus.COMPLETED &&
              it.results.size == 1 &&
              it.results[0].target.totalPayment.amount == BigDecimal("12000.00") &&
              it.results[0].target.monthlyInstallment.amount == BigDecimal("1000.00") &&
              it.results[0].target.totalInterest.amount == BigDecimal("2000.00")
          },
        )
      }
    }
}
