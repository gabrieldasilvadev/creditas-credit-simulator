package br.com.simulator.credit.creditas.exchangerate

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money

interface ExchangeRateService {
  fun convert(
    from: Money,
    to: Currency,
  ): Money
}
