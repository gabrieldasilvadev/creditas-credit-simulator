package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.command.SimulateLoanCommand
import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import com.trendyol.kediatr.Command
import java.util.UUID

class StartBulkSimulationCommand(
  val bulkId: UUID = UUID.randomUUID(),
  val simulations: List<LoanSimulationCommandDto>,
) : Command

data class LoanSimulationCommandDto(
  val loanAmount: Money,
  val customerInfo: CustomerInfo,
  val months: Int,
  val policyType: InterestRatePolicy,
  val sourceCurrency: String,
  val targetCurrency: String,
) {
  companion object {
    fun LoanSimulationCommandDto.toSimulateLoanCommand() =
      SimulateLoanCommand(
        amount = this.loanAmount,
        customerInfo = this.customerInfo,
        termInMonths = Months(this.months),
        policyType = this.policyType,
        sourceCurrency = Currency(this.sourceCurrency),
        targetCurrency = Currency(this.targetCurrency),
      )
  }
}
