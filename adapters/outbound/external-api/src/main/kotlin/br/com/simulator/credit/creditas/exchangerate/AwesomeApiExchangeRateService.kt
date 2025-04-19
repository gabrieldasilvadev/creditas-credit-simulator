package br.com.simulator.credit.creditas.exchangerate

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.exchangerate.client.AwesomeApiClient
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
@Monitorable("AwesomeApiExchangeRateService")
class AwesomeApiExchangeRateService(
  private val awesomeApiClient: AwesomeApiClient,
) : ExchangeRateService {
  private val logger = LoggerFactory.getLogger(AwesomeApiExchangeRateService::class.java)

  @Cacheable(
    value = ["exchangeRateCache"],
    key = "#from.currency.toString() + #to.toString()",
    unless = "#result == null",
  )
  @Retryable(
    value = [Exception::class],
    maxAttempts = 2,
    backoff = Backoff(delay = 1500, multiplier = 2.0),
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
        ?: throw IllegalStateException("Failure when seeking fee for $key")

    val rate =
      askRate.toBigDecimalOrNull()
        ?: throw IllegalStateException("Invalid rate: '$askRate' to $key")

    return Money(from.amount.multiply(rate), to)
  }

  @Recover
  fun fallbackConvert(
    ex: Exception,
    from: Money,
    to: Currency,
  ): Money {
    logger.error("Failure to seek exchange rate after multiple attempts. Fallback activated.", ex)

    return Money(from.amount, to)
  }
}
