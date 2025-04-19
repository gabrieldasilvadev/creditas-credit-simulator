package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class SimulateLoanAggregateTest {

  @Test
  fun `should create SimulateLoanAggregate with calculated result`() {
    val result = SimulationResult(
      totalPayment = Money("12000.00", Currency.USD),
      monthlyInstallment = Money("1000.00", Currency.USD),
      totalInterest = Money("2000.00", Currency.USD)
    )

    val simulation = mockk<Simulation>()
    every { simulation.calculate() } returns result

    val customerInfo = CustomerInfo(
      birthDate = LocalDate.of(1990, 1, 1),
      customerEmail = "cliente@teste.com"
    )

    val aggregate = SimulateLoanAggregate.of(simulation, customerInfo)

    assertNotNull(aggregate.id)
    assertEquals(simulation, aggregate.simulation)
    assertEquals(result, aggregate.simulationResult)
    assertEquals(customerInfo, aggregate.customerInfo)
  }
}
