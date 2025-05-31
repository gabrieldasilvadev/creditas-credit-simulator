package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
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
        annualRate = Money("0.03"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment).isEqualTo(BigDecimal("846.94").toMoney())
    assertThat(result.totalPayment).isEqualTo(BigDecimal("10163.28").toMoney())
    assertThat(result.totalInterest).isEqualTo(BigDecimal("163.28").toMoney())
  }

  @Test
  fun `should calculate simulation with zero interest rate`() {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(Money("12000.00")),
        months = Months(12),
        annualRate = Money("0.00"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment.amount).isEqualTo(BigDecimal("1000.00"))
    assertThat(result.totalPayment.amount).isEqualTo(BigDecimal("12000.00"))
    assertThat(result.totalInterest.amount).isEqualTo(BigDecimal("0.00"))
  }

  @Test
  fun `should calculate simulation for longer term (24 months)`() {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(Money("10000.00")),
        months = Months(24),
        annualRate = Money("0.03"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment.amount).isEqualTo(BigDecimal("429.81"))
    assertThat(result.totalPayment.amount).isEqualTo(BigDecimal("10315.44"))
    assertThat(result.totalInterest.amount).isEqualTo(BigDecimal("315.44"))
  }

  @Test
  fun `should calculate simulation for higher loan amount`() {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(Money("50000.00")),
        months = Months(12),
        annualRate = Money("0.03"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment.amount).isEqualTo(BigDecimal("4234.68"))
    assertThat(result.totalPayment.amount).isEqualTo(BigDecimal("50816.16"))
    assertThat(result.totalInterest.amount).isEqualTo(BigDecimal("816.16"))
  }

  @Test
  fun `should calculate simulation with higher interest rate`() {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(Money("10000.00")),
        months = Months(12),
        annualRate = Money("0.10"),
      )

    val result = simulation.calculate()

    assertThat(result.monthlyInstallment).isEqualTo(Money("879.16"))
    assertThat(result.totalPayment).isEqualTo(Money("10549.92"))
    assertThat(result.totalInterest).isEqualTo(Money("549.92"))
  }

  @Test
  fun `should not allow simulation with negative value`() {
    assertThrows(IllegalArgumentException::class.java) {
      Simulation.create(
        loanAmount = LoanAmount(Money("-1000")),
        months = Months(12),
        annualRate = Money("0.03"),
      )
    }
  }
}
