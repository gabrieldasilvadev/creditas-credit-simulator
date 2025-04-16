package br.com.simulator.credit.creditas.exchangerate

import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money

interface ExchangeRateService {
  fun convert(
    from: Money,
    to: Currency,
  ): Money
}
