package br.com.simulator.credit.creditas.command.factory

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.exchangerate.ExchangeRateService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class LoanAmountFactoryTest {
  private val exchangeRateService = mockk<ExchangeRateService>()
  private lateinit var factory: LoanAmountFactory

  @BeforeEach
  fun setup() {
    factory = LoanAmountFactory(exchangeRateService)
  }

  @Test
  fun `should return original amount when currencies are equal`() {
    val amount = BigDecimal("1000.00")
    val currency = Currency.BRL
    val money = Money(amount, currency)

    val result =
      factory.create(
        amount = money,
        source = currency,
        target = currency,
      )

    assertEquals(money, result.value)
    verify(exactly = 0) {
      exchangeRateService.convert(
        Money(BigDecimal("1000.00"), Currency.USD),
        Currency.USD,
      )
    }
  }

  @Test
  fun `should convert amount when currencies are different`() {
    val original = Money(BigDecimal("1000.00"), Currency.USD)
    val converted = Money(BigDecimal("5200.00"), Currency.BRL)

    every { exchangeRateService.convert(original, Currency.BRL) } returns converted

    val result = factory.create(original, Currency.USD, Currency.BRL)

    assertEquals(converted, result.value)
    verify(exactly = 1) { exchangeRateService.convert(original, Currency.BRL) }
  }

  @Test
  fun `should default to BRL when source and target are null`() {
    val money = Money(BigDecimal("1500.00"), Currency.BRL)

    val result =
      factory.create(
        amount = money,
        source = null,
        target = null,
      )

    assertEquals(money, result.value)
    verify(exactly = 0) {
      exchangeRateService.convert(
        Money(BigDecimal("1000.00"), Currency.USD),
        Currency.USD,
      )
    }
  }
}
