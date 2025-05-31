package br.com.simulator.credit.creditas.commondomain.valueobjects

import br.com.simulator.credit.creditas.commondomain.AMOUNT_SCALE
import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
  val amount: BigDecimal,
  val currency: Currency = Currency.BRL,
) {
  constructor(
    amount: String,
    currency: Currency = Currency.BRL,
  ) : this(
    BigDecimal(amount),
    currency,
  )

  init {
    require(amount >= BigDecimal.ZERO) {
      "Loan amount must be greater than zero."
    }
  }

  fun convertTo(
    target: Currency,
    rate: BigDecimal,
  ): Money {
    return if (currency == target) {
      this
    } else {
      Money(amount.multiply(rate), target)
    }
  }

  operator fun plus(other: Money): Money {
    require(currency == other.currency) { "Currency mismatch on addition" }
    return Money(amount + other.amount, currency)
  }

  operator fun minus(other: Money): Money {
    require(currency == other.currency) { "Currency mismatch on subtraction" }
    return Money(amount - other.amount, currency)
  }

  operator fun times(multiplier: BigDecimal): Money {
    return Money(amount.multiply(multiplier), currency)
  }

  fun toBigDecimal(): BigDecimal {
    return amount
  }

  fun rounded(
    scale: Int = AMOUNT_SCALE,
    roundingMode: RoundingMode = RoundingMode.HALF_EVEN,
  ): Money {
    return Money(amount.setScale(scale, roundingMode), currency)
  }

  fun toView(scale: Int = 2): String {
    return "${currency.symbol}${amount.setScale(scale, RoundingMode.HALF_EVEN)}"
  }

  override fun toString(): String {
    return "$amount $currency"
  }
}
