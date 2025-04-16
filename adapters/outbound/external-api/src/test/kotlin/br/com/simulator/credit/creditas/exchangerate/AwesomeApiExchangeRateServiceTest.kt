package br.com.simulator.credit.creditas.exchangerate

import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.exchangerate.client.AwesomeApiClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AwesomeApiExchangeRateServiceTest {
  private val awesomeApiClient = mockk<AwesomeApiClient>()
  private val service = AwesomeApiExchangeRateService(awesomeApiClient)

  private val usd = Currency("USD")
  private val brl = Currency("BRL")

  @Test
  fun `should return same money when currencies are equal`() {
    val money = Money(BigDecimal("100.00"), usd)

    val result = service.convert(money, usd)

    assertEquals(money, result)
    verify(exactly = 0) { awesomeApiClient.getRate(any(), any()) }
  }

  @Test
  fun `should convert money when currencies are different`() {
    val mockResponse = mapOf("USDBRL" to mapOf("ask" to "5.25"))

    every { awesomeApiClient.getRate("USD", "BRL") } returns mockResponse

    val input = Money(BigDecimal("100.00"), usd)
    val result = service.convert(input, brl)

    assertEquals(brl, result.currency)
    assertEquals(0, result.amount.compareTo(BigDecimal("525.00")))
  }

  @Test
  fun `should throw exception when ask rate is missing`() {
    val mockResponse = mapOf("USDBRL" to emptyMap<String, String>())

    every { awesomeApiClient.getRate("USD", "BRL") } returns mockResponse

    val input = Money(BigDecimal("100.00"), usd)

    val ex =
      assertThrows(IllegalStateException::class.java) {
        service.convert(input, brl)
      }

    assertTrue(ex.message!!.contains("Falha ao buscar taxa"))
  }

  @Test
  fun `should throw exception when ask rate is invalid`() {
    val mockResponse = mapOf("USDBRL" to mapOf("ask" to "INVALID"))

    every { awesomeApiClient.getRate("USD", "BRL") } returns mockResponse

    val input = Money(BigDecimal("100.00"), usd)

    val ex =
      assertThrows(IllegalStateException::class.java) {
        service.convert(input, brl)
      }

    assertTrue(ex.message!!.contains("Taxa inválida"))
  }
}
