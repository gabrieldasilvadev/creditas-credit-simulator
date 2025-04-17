package br.com.simulator.credit.creditas.commondomain

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CurrencyTest {
  @Test
  fun `should return correct symbol for BRL`() {
    val currency = Currency("BRL")
    assertEquals("R$", currency.symbol)
  }

  @Test
  fun `should return correct symbol for USD`() {
    val currency = Currency("USD")
    assertEquals("$", currency.symbol)
  }

  @Test
  fun `should return correct symbol for EUR`() {
    val currency = Currency("EUR")
    assertEquals("€", currency.symbol)
  }

  @Test
  fun `should reject invalid ISO code`() {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        Currency("us")
      }
    assertEquals("Invalid currency code: us", exception.message)
  }

  @Test
  fun `should reject empty symbol using reflection`() {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        Currency(StringUtils.EMPTY)
      }
    assertTrue(exception.message!!.contains("Invalid currency code: "))
  }
}
