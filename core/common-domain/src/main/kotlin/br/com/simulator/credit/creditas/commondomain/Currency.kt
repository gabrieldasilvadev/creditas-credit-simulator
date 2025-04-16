package br.com.simulator.credit.creditas.commondomain

import java.util.Currency as JavaCurrency

@JvmInline
value class Currency(val code: String) {
  init {
    require(code.matches(Regex("^[A-Z]{3}$"))) { "Invalid currency code: $code" }
  }

  val symbol: String
    get() = JavaCurrency.getInstance(code).symbol

  companion object {
    val BRL = Currency("BRL")
    val USD = Currency("USD")
    val EUR = Currency("EUR")
  }

  override fun toString(): String = code
}
