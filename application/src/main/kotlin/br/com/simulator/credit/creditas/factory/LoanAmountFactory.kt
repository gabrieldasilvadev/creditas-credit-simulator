package br.com.simulator.credit.creditas.factory

import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.exchangerate.ExchangeRateService
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import org.springframework.stereotype.Component

@Component
class LoanAmountFactory(
  private val exchangeRateService: ExchangeRateService,
) {
  fun create(
    amount: Money,
    source: Currency?,
    target: Currency?,
  ): LoanAmount {
    val from = source ?: Currency.BRL
    val to = target ?: Currency.BRL

    val original = Money(amount.toBigDecimal(), from)
    val converted =
      if (from != to) {
        exchangeRateService.convert(original, to)
      } else {
        original
      }
    return LoanAmount(converted)
  }
}
