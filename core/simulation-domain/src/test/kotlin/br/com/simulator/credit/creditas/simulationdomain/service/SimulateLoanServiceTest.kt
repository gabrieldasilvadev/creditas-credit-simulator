package br.com.simulator.credit.creditas.simulationdomain.service

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import br.com.simulator.credit.creditas.simulationdomain.model.Simulation
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SimulateLoanServiceTest {

  private val loanAmount = Money(BigDecimal("10000.00"), Currency.BRL)
  private val months = Months(12)
  private val interestRate = Money(BigDecimal("0.03"), Currency.BRL)
  private val customer = CustomerInfo(
    birthDate = LocalDate.of(1990, 1, 1),
    customerEmail = "cliente@teste.com"
  )

  private lateinit var simulationData: LoanSimulationData

  @BeforeEach
  fun setUp() {
    simulationData = LoanSimulationData(
      loanAmount = loanAmount,
      duration = months,
      applicant = customer,
      annualInterestRate = interestRate
    )
  }

  @Test
  fun `should execute simulation and register completion event`() {
    val simulation = mockk<Simulation>(relaxed = true)
    val simulationResult = SimulationResult(
      totalPayment = Money(BigDecimal("12000.00"), Currency.BRL),
      monthlyInstallment = Money(BigDecimal("1000.00"), Currency.BRL),
      totalInterest = Money(BigDecimal("2000.00"), Currency.BRL)
    )

    mockkObject(Simulation)
    every { Simulation.create(any(), any(), any()) } returns simulation
    every { simulation.calculate() } returns simulationResult
    every { simulation.registerEvent(any()) } just Runs

    mockkObject(LoanSimulationInputDataEvent.Companion)
    mockkObject(SimulationResultEvent.Companion)
    every { LoanSimulationInputDataEvent.from(any()) } returns mockk()
    every { SimulationResultEvent.from(simulationResult) } returns mockk()

    val result = SimulateLoanService.execute(simulationData)

    verify { Simulation.create(any(), any(), any()) }
    verify { simulation.calculate() }
    verify { simulation.registerEvent(ofType(SimulationCompletedEvent::class)) }

    assert(result === simulation)

    unmockkAll()
  }
}
