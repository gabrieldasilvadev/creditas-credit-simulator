package br.com.simulator.credit.creditas.exchangerate

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.exchangerate.client.AwesomeApiClient
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
@Monitorable("AwesomeApiExchangeRateService")
class AwesomeApiExchangeRateService(
  private val awesomeApiClient: AwesomeApiClient,
) : ExchangeRateService {

  @Cacheable(
    value = ["exchangeRateCache"],
    key = "#from.currency.toString() + #to.toString()",
    unless = "#result == null"
  )
  override fun convert(
    from: Money,
    to: Currency,
  ): Money {
    requireNotNull(from.currency) { "Currency of origin cannot be null" }
    requireNotNull(to.code) { "Destination currency cannot be null" }

    if (from.currency == to) return from

    val key = "${from.currency}$to"
    val response = awesomeApiClient.getRate(from.currency.code, to.code)
    val askRate =
      response[key]?.get("ask")
        ?: throw IllegalStateException("Falha ao buscar taxa para $key")

    val rate =
      askRate.toBigDecimalOrNull()
        ?: throw IllegalStateException("Taxa inválida: '$askRate' para $key")

    return Money(from.amount.multiply(rate), to)
  }
}
