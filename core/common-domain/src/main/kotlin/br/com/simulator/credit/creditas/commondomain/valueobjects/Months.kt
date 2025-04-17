package br.com.simulator.credit.creditas.commondomain.valueobjects

import java.math.BigDecimal

@JvmInline
value class Months(val value: Int) {

  inline val asBigDecimal: BigDecimal get() = BigDecimal.valueOf(value.toLong())
}
