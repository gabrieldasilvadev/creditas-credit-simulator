package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.AMOUNT_SCALE
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class LoanAmountTest {

  @Test
  fun `should throw exception when amount is zero or negative`() {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      LoanAmount(Money(BigDecimal.ZERO, Currency.BRL))
    }

    assertEquals("Loan amount must be greater than zero.", exception.message)
  }

  @Test
  fun `should calculate installment with zero interest`() {
    val loanAmount = LoanAmount(Money("12000.00", Currency.BRL))
    val months = Months(12)
    val rate = BigDecimal.ZERO

    val installment = loanAmount.monthlyInstallment(rate, months)

    assertEquals(
      Money(BigDecimal("1000.00").setScale(AMOUNT_SCALE, RoundingMode.HALF_EVEN), Currency.BRL),
      installment
    )
  }

  @Test
  fun `should calculate installment with interest`() {
    val loanAmount = LoanAmount(Money(BigDecimal("10000.00"), Currency.BRL))
    val months = Months(12)
    val annualRate = BigDecimal("0.06")
    val monthlyRate = annualRate.divide(BigDecimal(12), 10, RoundingMode.HALF_EVEN)

    val installment = loanAmount.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("860.66").setScale(AMOUNT_SCALE, RoundingMode.HALF_EVEN)
    assertEquals(Money(expected, Currency.BRL), installment)
  }
}
