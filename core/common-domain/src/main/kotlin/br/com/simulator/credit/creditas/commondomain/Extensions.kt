package br.com.simulator.credit.creditas.commondomain

import org.instancio.Select
import org.instancio.TargetSelector
import java.lang.reflect.Field
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun <T, V> kfield(property: KProperty1<T, V>): TargetSelector {
  val field: Field =
    requireNotNull(property.javaField) {
      "Property '${property.name}' must be a backing field"
    }
  return Select.field(field.declaringClass, field.name)
}

fun BigDecimal.pmt(
  rate: BigDecimal,
  months: Int,
): BigDecimal {
  val factor = (BigDecimal.ONE + rate).pow(months, MATH_CONTEXT)
  val denominator = factor - BigDecimal.ONE
  require(denominator > BigDecimal.ZERO) { "Invalid denominator" }

  return this
    .multiply(rate, MATH_CONTEXT)
    .multiply(factor, MATH_CONTEXT)
    .divide(
      denominator,
      AMOUNT_SCALE,
      RoundingMode.HALF_EVEN,
    )
}

fun BigDecimal.toMoney(currency: Currency = Currency.BRL): Money {
  if (this == BigDecimal.ZERO) {
    return Money(this)
  }

  return Money(
    amount = this.setScale(AMOUNT_SCALE, RoundingMode.HALF_EVEN),
    currency = currency,
  )
}

fun String.toMoney(currency: Currency = Currency.BRL): Money {
  return this.toBigDecimalOrNull()?.toMoney(currency)
    ?: throw IllegalArgumentException("Invalid money value: $this")
}
