package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainObject
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import java.math.BigDecimal
import java.math.MathContext

private val MONTHS_IN_YEAR = Months(12).asBigDecimal

class Simulation private constructor(
  val loanAmount: LoanAmount,
  val months: Months,
  val monthlyRate: BigDecimal,
) : DomainObject() {

  fun calculate(): SimulationResult {
    val monthlyInstallment = loanAmount.monthlyInstallment(monthlyRate, months)
    val total = monthlyInstallment * months.asBigDecimal
    val interest = total - loanAmount.value

    val simulationResult = SimulationResult(total, monthlyInstallment, interest)

    return simulationResult
  }

  companion object {
    fun create(
      loanAmount: LoanAmount,
      months: Months,
      annualRate: Money,
    ): Simulation {
      val monthlyRateFromAnnual = annualRate.toBigDecimal().divide(MONTHS_IN_YEAR, MathContext.DECIMAL128)
      return Simulation(loanAmount, months, monthlyRateFromAnnual)
    }
  }
}
