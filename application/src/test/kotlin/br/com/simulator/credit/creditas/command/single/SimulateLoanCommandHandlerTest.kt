package br.com.simulator.credit.creditas.command.single

import br.com.simulator.credit.creditas.command.SimulationSemaphoreProvider
import br.com.simulator.credit.creditas.command.dto.LoanSimulationHttpResponse.Source
import br.com.simulator.credit.creditas.command.dto.LoanSimulationHttpResponse.Target
import br.com.simulator.credit.creditas.command.factory.LoanAmountFactory
import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEventPublisher
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.LoanSimulationId
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.Simulation
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.service.SimulateLoanService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SimulateLoanCommandHandlerTest {
  private val publisher = mockk<DomainEventPublisher>(relaxed = true)
  private val loanAmountFactory = mockk<LoanAmountFactory>()
  private val persistencePort = mockk<SimulationPersistencePort>()
  private val simulationSemaphoreProvider = mockk<SimulationSemaphoreProvider>(relaxed = true)

  private lateinit var handler: SimulateLoanCommandHandler

  @BeforeEach
  fun setup() {
    handler =
      SimulateLoanCommandHandler(
        publisher,
        loanAmountFactory,
        persistencePort,
        simulationSemaphoreProvider,
      )
  }

  @Test
  fun `should execute simulation and return response`() =
    runBlocking {
      val amount = Money(BigDecimal("10000.00"), Currency.BRL)
      val convertedAmount = Money(BigDecimal("10000.00"), Currency.USD)
      val interestRate = Money(BigDecimal("0.03"), Currency.BRL)
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")
      val loanAmount = LoanAmount(convertedAmount)
      val result =
        SimulationResult(
          totalPayment = Money(BigDecimal("12000.00"), Currency.USD),
          monthlyInstallment = Money(BigDecimal("1000.00"), Currency.USD),
          totalInterest = Money(BigDecimal("2000.00"), Currency.USD),
        )

      val simulation =
        mockk<Simulation> {
          every { calculate() } returns result
          every { getAndClearEvents() } returns emptyList()
        }

      val aggregate =
        SimulateLoanAggregate(
          id = LoanSimulationId(),
          simulation = simulation,
          simulationResult = result,
          customerInfo = customerInfo,
        )

      val policy =
        mockk<InterestRatePolicy> {
          every { annualInterestRate(any()) } returns BigDecimal("0.03")
        }

      val command =
        SimulateLoanCommand(
          amount = amount,
          termInMonths = Months(12),
          customerInfo = customerInfo,
          sourceCurrency = Currency.BRL,
          targetCurrency = Currency.USD,
          interestRatePolicy = policy,
        )

      every {
        loanAmountFactory.create(any(), Currency.BRL, Currency.USD)
      } returns loanAmount

      mockkObject(SimulateLoanService)
      every { SimulateLoanService.execute(any()) } returns simulation

      mockkObject(SimulateLoanAggregate.Companion)
      every { SimulateLoanAggregate.of(simulation, customerInfo) } returns aggregate

      every { persistencePort.save(aggregate) } just Runs

      val response = handler.handle(command)

      verify { loanAmountFactory.create(amount, Currency.BRL, Currency.USD) }
      verify { SimulateLoanService.execute(any()) }
      verify { SimulateLoanAggregate.of(simulation, customerInfo) }
      verify { persistencePort.save(aggregate) }
      verify { publisher.publishAll(emptyList()) }

      assert(response.source == Source(amount))
      assert(
        response.target ==
          Target(
            convertedAmount = convertedAmount,
            totalPayment = result.totalPayment,
            monthlyInstallment = result.monthlyInstallment,
            totalInterest = result.totalInterest,
            annualInterestRate = interestRate,
          ),
      )
      unmockkAll()
    }
}
