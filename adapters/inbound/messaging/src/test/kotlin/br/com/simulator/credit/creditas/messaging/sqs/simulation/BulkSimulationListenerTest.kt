package br.com.simulator.credit.creditas.messaging.sqs.simulation

import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toLoanSimulationCommandDto
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto.Companion.toSimulateLoanCommand
import br.com.simulator.credit.creditas.command.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import com.trendyol.kediatr.Mediator
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BulkSimulationListenerTest {
  private val repository = mockk<BulkSimulationPersistenceAdapter>(relaxed = true)
  private val mediator = mockk<Mediator>(relaxed = true)
  private val policyConfiguration =
    mockk<br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration>(relaxed = true)
  private val acknowledgment = mockk<Acknowledgement>(relaxed = true)

  private lateinit var listener: BulkSimulationListener

  @BeforeEach
  fun setup() {
    listener = BulkSimulationListener(repository, mediator, policyConfiguration)
  }

  @Test
  fun `should process simulations and acknowledge message`() = runBlocking {
    val bulkId = UUID.randomUUID()
    val policyType = PolicyType.AGE_BASED
    val loanAmount = Money(BigDecimal("10000.00"), Currency.BRL)
    val simulationMsg = BulkSimulationMessage.LoanSimulationMessage(
      loanAmount = loanAmount,
      customerInfo = CustomerInfo(
        customerEmail = "cliente1@teste.com",
        birthDate = LocalDate.of(1990, 1, 1)
      ),
      months = 12,
      interestRate = Money(BigDecimal("0.03")),
      sourceCurrency = "BRL",
      targetCurrency = "USD",
      policyType = policyType
    )
    val message = BulkSimulationMessage(bulkId, listOf(simulationMsg))

    val interestRatePolicy = mockk<InterestRatePolicy>()
    val command = simulationMsg
      .toLoanSimulationCommandDto(policyType)
      .toSimulateLoanCommand(interestRatePolicy)

    val response = LoanSimulationHttpResponse(
      source = LoanSimulationHttpResponse.Source(amount = loanAmount),
      target = LoanSimulationHttpResponse.Target(
        convertedAmount = Money(BigDecimal("10000.00"), Currency.USD),
        totalPayment = Money(BigDecimal("12000.00"), Currency.USD),
        monthlyInstallment = Money(BigDecimal("1000.00"), Currency.USD),
        totalInterest = Money(BigDecimal("2000.00"), Currency.USD),
        annualInterestRate = Money(BigDecimal("0.03"), Currency.USD)
      )
    )

    every { policyConfiguration.resolve(policyType) } returns interestRatePolicy
    coEvery { mediator.send(command) } returns response

    listener.onMessage(message, acknowledgment)
    delay(100)

    verify(exactly = 1) {
      repository.updateIncrementAndPushResult(
        bulkId,
        match { dto ->
          dto.source.amount == loanAmount &&
            dto.target.totalPayment.amount == BigDecimal("12000.00") &&
            dto.target.monthlyInstallment.amount == BigDecimal("1000.00") &&
            dto.target.totalInterest.amount == BigDecimal("2000.00")
        },
        true
      )
    }
    verify(exactly = 1) { acknowledgment.acknowledge() }
  }
}
