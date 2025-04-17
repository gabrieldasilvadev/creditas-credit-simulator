package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months

data class LoanSimulationData(
  val loanAmount: Money,
  val duration: Months,
  val applicant: CustomerInfo,
) {
  companion object {
    fun from(
      loanAmount: Money,
      duration: Months,
      applicant: CustomerInfo,
    ): LoanSimulationData {
      return LoanSimulationData(
        loanAmount = loanAmount,
        duration = duration,
        applicant = applicant,
      )
    }
  }
}
