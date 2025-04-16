package br.com.simulator.credit.creditas.commondomain.valueobjects

import java.math.BigDecimal

@JvmInline
value class Months(val value: Int) {
  init {
    require(value in 1..12) { "Months must be between 1 and 12." }
  }

  inline val asBigDecimal: BigDecimal get() = BigDecimal.valueOf(value.toLong())
}
