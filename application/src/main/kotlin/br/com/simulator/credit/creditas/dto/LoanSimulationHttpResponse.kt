package br.com.simulator.credit.creditas.dto

import br.com.simulator.credit.creditas.commondomain.Money
import java.math.BigDecimal

data class LoanSimulationHttpResponse(
  val source: Source,
  val target: Target,
) {
  data class Source(
    val amount: Money,
  )

  data class Target(
    val convertedAmount: Money,
    val totalPayment: Money,
    val monthlyInstallment: Money,
    val totalInterest: Money,
    val annualInterestRate: BigDecimal,
  )
}
