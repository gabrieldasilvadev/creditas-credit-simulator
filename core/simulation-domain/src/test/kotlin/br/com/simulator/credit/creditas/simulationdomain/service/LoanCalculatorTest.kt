package br.com.simulator.credit.creditas.simulationdomain.service

import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

internal class LoanCalculatorTest {
  @Test
  fun `should calculate simulation correctly for 3 percent annual rate`() {
    val result =
      LoanCalculator.simulate(
        loanAmount = BigDecimal("10000.00").toMoney(),
        months = Months(12),
        annualRate = BigDecimal("0.03").toMoney(),
      )

    assertThat(result.monthlyInstallment).isEqualTo(Money("846.94"))
    assertThat(result.totalPayment).isEqualTo(Money("10163.28"))
    assertThat(result.totalInterest).isEqualTo(Money("163.28"))
  }

  @Test
  fun `should calculate simulation with zero interest rate`() {
    val result =
      LoanCalculator.simulate(
        loanAmount = Money("12000.00"),
        months = Months(12),
        annualRate = BigDecimal.ZERO.toMoney(),
      )

    assertThat(result.monthlyInstallment).isEqualTo(Money("1000.00"))
    assertThat(result.totalPayment).isEqualTo(Money("12000.00"))
    assertThat(result.totalInterest).isEqualTo(Money("0.00"))
  }

  @Test
  fun `should throw exception when loan amount is zero`() {
    val exception =
      assertThrows<IllegalArgumentException> {
        LoanCalculator.simulate(
          loanAmount = BigDecimal.ZERO.toMoney(),
          months = Months(12),
          annualRate = BigDecimal("0.05").toMoney(),
        )
      }

    assertThat(exception.message).containsIgnoringCase("greater than zero")
  }
}
