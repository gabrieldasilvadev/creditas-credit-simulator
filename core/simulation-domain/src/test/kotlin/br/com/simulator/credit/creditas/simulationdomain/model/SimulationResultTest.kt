package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SimulationResultTest {
  @Test
  fun `should expose correct values`() {
    val total = Money(BigDecimal("12000.00"), Currency.BRL)
    val installment = Money(BigDecimal("1000.00"), Currency.BRL)
    val interest = Money(BigDecimal("2000.00"), Currency.BRL)

    val result =
      SimulationResult(
        totalPayment = total,
        monthlyInstallment = installment,
        totalInterest = interest,
      )

    assertEquals(total, result.totalPayment)
    assertEquals(installment, result.monthlyInstallment)
    assertEquals(interest, result.totalInterest)
  }
}
