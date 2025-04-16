package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import br.com.simulator.credit.creditas.commondomain.AMOUNT_SCALE
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import java.math.BigDecimal
import java.math.RoundingMode

data class LoanAmount(
  val value: Money,
) {
  init {
    require(value.amount > BigDecimal.ZERO) { "Loan amount must be greater than zero." }
  }

  fun monthlyInstallment(
    monthlyRate: BigDecimal,
    months: Months,
  ): Money {
    if (monthlyRate == BigDecimal.ZERO) {
      val monthly = value.amount.divide(months.asBigDecimal, AMOUNT_SCALE, RoundingMode.HALF_EVEN)
      return Money(monthly, value.currency)
    }

    val one = BigDecimal.ONE
    val factor = one + monthlyRate
    val numerator = value.amount * monthlyRate * factor.pow(months.value)
    val denominator = factor.pow(months.value) - one
    val installment = numerator.divide(denominator, AMOUNT_SCALE, RoundingMode.HALF_EVEN)

    return Money(installment, value.currency)
  }
}
