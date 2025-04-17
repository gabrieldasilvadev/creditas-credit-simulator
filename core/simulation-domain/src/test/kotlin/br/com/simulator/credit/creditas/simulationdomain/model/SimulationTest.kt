package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import java.math.BigDecimal
import kotlin.test.Test

internal class SimulationTest {
  @Test
  fun `should calculate simulation correctly to R$10000, 12 months, 3 percent per year`() {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(Money("10000.00")),
        months = Months(12),
        annualRate = BigDecimal("0.03"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment).isEqualTo(BigDecimal("846.94").toMoney())
    assertThat(result.totalPayment).isEqualTo(BigDecimal("10163.28").toMoney())
    assertThat(result.totalInterest).isEqualTo(BigDecimal("163.28").toMoney())
  }

  @Test
  fun `should not allow simulation with negative value`() {
    assertThrows(IllegalArgumentException::class.java) {
      Simulation.create(
        loanAmount = LoanAmount(Money("-1000")),
        months = Months(12),
        annualRate = BigDecimal("0.03"),
      )
    }
  }
}
