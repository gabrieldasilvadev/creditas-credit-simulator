package br.com.simulator.credit.creditas.simulationdomain.valueobjects

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

internal class LoanAmountTest {
  @Test
  fun `should calculate monthly installment with zero interest`() {
    val loan = LoanAmount(Money(BigDecimal("1200.00"), Currency.BRL))
    val months = Months(12)
    val monthly = loan.monthlyInstallment(BigDecimal.ZERO, months)

    assertEquals(BigDecimal("100.00"), monthly.amount)
    assertEquals(Currency.BRL, monthly.currency)
  }

  @Test
  fun `should calculate monthly installment with interest`() {
    val loan = LoanAmount(Money(BigDecimal("10000.00"), Currency.USD))
    val months = Months(12)
    val monthlyRate = BigDecimal("0.01")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("888.49")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.USD, result.currency)
  }

  @Test
  fun `should calculate monthly installment for longer term (24 months)`() {
    val loan = LoanAmount(Money(BigDecimal("10000.00"), Currency.BRL))
    val months = Months(24)
    val monthlyRate = BigDecimal("0.01")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("470.73")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should calculate monthly installment for shorter term (6 months)`() {
    val loan = LoanAmount(Money(BigDecimal("10000.00"), Currency.BRL))
    val months = Months(6)
    val monthlyRate = BigDecimal("0.01")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("1725.48")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should calculate monthly installment with higher interest rate`() {
    val loan = LoanAmount(Money(BigDecimal("10000.00"), Currency.BRL))
    val months = Months(12)
    val monthlyRate = BigDecimal("0.02")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("945.60")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should calculate monthly installment with very low interest rate`() {
    val loan = LoanAmount(Money(BigDecimal("10000.00"), Currency.BRL))
    val months = Months(12)
    val monthlyRate = BigDecimal("0.001")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("838.84")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should calculate monthly installment for different loan amount`() {
    val loan = LoanAmount(Money(BigDecimal("5000.00"), Currency.BRL))
    val months = Months(12)
    val monthlyRate = BigDecimal("0.01")
    val result = loan.monthlyInstallment(monthlyRate, months)

    val expected = BigDecimal("444.24")
    val rounded = result.amount.setScale(2, RoundingMode.HALF_EVEN)

    assertEquals(expected, rounded)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should reject zero amount`() {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        LoanAmount(Money(BigDecimal.ZERO, Currency.BRL))
      }
    assertEquals("Loan amount must be greater than zero.", exception.message)
  }

  @Test
  fun `should reject negative amount`() {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        LoanAmount(Money(BigDecimal("-100.00"), Currency.BRL))
      }
    assertEquals("Loan amount must be greater than zero.", exception.message)
  }
}
