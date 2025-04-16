package br.com.simulator.credit.creditas.commondomain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class MoneyTest {
  @Test
  fun `should convert to target currency using given rate`() {
    val money = Money(BigDecimal("100.00"), Currency.USD)
    val result = money.convertTo(Currency.BRL, BigDecimal("5.50"))

    assertEquals(BigDecimal("550.00").stripTrailingZeros(), result.amount.stripTrailingZeros())
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should not convert if currency is the same`() {
    val money = Money(BigDecimal("100.00"), Currency.USD)
    val result = money.convertTo(Currency.USD, BigDecimal("1.23"))

    assertSame(money, result)
  }

  @Test
  fun `should add two money values with the same currency`() {
    val a = Money(BigDecimal("50.00"), Currency.EUR)
    val b = Money(BigDecimal("70.00"), Currency.EUR)

    val result = a + b

    assertEquals(BigDecimal("120.00"), result.amount)
    assertEquals(Currency.EUR, result.currency)
  }

  @Test
  fun `should subtract two money values with the same currency`() {
    val a = Money(BigDecimal("200.00"), Currency.BRL)
    val b = Money(BigDecimal("50.00"), Currency.BRL)

    val result = a - b

    assertEquals(BigDecimal("150.00"), result.amount)
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should throw when adding money with different currencies`() {
    val a = Money(BigDecimal("10.00"), Currency.USD)
    val b = Money(BigDecimal("5.00"), Currency.EUR)

    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        a + b
      }

    assertEquals("Currency mismatch on addition", exception.message)
  }

  @Test
  fun `should throw when subtracting money with different currencies`() {
    val a = Money(BigDecimal("10.00"), Currency.USD)
    val b = Money(BigDecimal("5.00"), Currency.BRL)

    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        a - b
      }

    assertEquals("Currency mismatch on subtraction", exception.message)
  }

  @Test
  fun `should multiply money value by a factor`() {
    val money = Money(BigDecimal("100.00"), Currency.BRL)
    val result = money * BigDecimal("1.5")

    assertEquals(BigDecimal("150.00").stripTrailingZeros(), result.amount.stripTrailingZeros())
    assertEquals(Currency.BRL, result.currency)
  }

  @Test
  fun `should render as string with symbol and two decimal places`() {
    val money = Money(BigDecimal("1234.5678"), Currency.EUR)
    assertEquals("€1234.57", money.toView())
  }

  @Test
  fun `should fail if amount is negative`() {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        Money(BigDecimal("-1.00"), Currency.USD)
      }
    assertEquals("Amount must be non-negative", exception.message)
  }
}
