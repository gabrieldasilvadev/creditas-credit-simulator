package br.com.simulator.credit.creditas.infrastructure

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.infrastructure.events.SpringDomainEventPublisher
import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.math.BigDecimal
import java.time.LocalDate

internal class SpringDomainEventPublisherTest {
  private val mockPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
  private val domainPublisher = SpringDomainEventPublisher(mockPublisher)

  @Test
  fun `should publish SimulationCompletedEvent`() =
    runTest {
      val application =
        LoanSimulationData(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          duration = Months(12),
          applicant = CustomerInfo(LocalDate.of(1990, 1, 1), "test@example.com"),
        )

      val result =
        SimulationResult(
          totalPayment = Money(BigDecimal("12000.00"), Currency.BRL),
          monthlyInstallment = Money(BigDecimal("1000.00"), Currency.BRL),
          totalInterest = Money(BigDecimal("2000.00"), Currency.BRL),
        )

      val event =
        SimulationCompletedEvent(
          LoanSimulationInputDataEvent.from(application),
          SimulationResultEvent.from(result),
          System.currentTimeMillis(),
          "12345",
        )

      domainPublisher.publish(event)

      verify { mockPublisher.publishEvent(event) }
    }
}
