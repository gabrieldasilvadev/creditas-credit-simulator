package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months

data class LoanSimulationData(
  val loanAmount: Money,
  val duration: Months,
  val applicant: CustomerInfo,
  val annualInterestRate: Money,
) {
  companion object {
    fun from(
      loanAmount: Money,
      duration: Months,
      applicant: CustomerInfo,
      annualInterestRate: Money,
    ): LoanSimulationData {
      return LoanSimulationData(
        loanAmount = loanAmount,
        duration = duration,
        applicant = applicant,
        annualInterestRate = annualInterestRate,
      )
    }
  }
}
