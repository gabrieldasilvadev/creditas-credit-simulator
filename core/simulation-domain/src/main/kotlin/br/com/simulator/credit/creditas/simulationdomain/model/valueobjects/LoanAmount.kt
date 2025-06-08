package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import br.com.simulator.credit.creditas.commondomain.AMOUNT_SCALE
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
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
    if (monthlyRate.compareTo(BigDecimal.ZERO) == 0 ||
        monthlyRate.abs().compareTo(BigDecimal("0.0000000001")) < 0) {
      val monthly = value.amount.divide(months.asBigDecimal, AMOUNT_SCALE, RoundingMode.HALF_EVEN)
      return Money(monthly, value.currency)
    }

    val one = BigDecimal.ONE
    val factor = one + monthlyRate
    val numerator = value.amount * monthlyRate * factor.pow(months.value)
    val denominator = factor.pow(months.value) - one

    if (denominator.compareTo(BigDecimal.ZERO) == 0) {
      val monthly = value.amount.divide(months.asBigDecimal, AMOUNT_SCALE, RoundingMode.HALF_EVEN)
      return Money(monthly, value.currency)
    }


    val installment = numerator.divide(denominator, AMOUNT_SCALE, RoundingMode.HALF_EVEN)

    return Money(installment, value.currency)
  }
}
