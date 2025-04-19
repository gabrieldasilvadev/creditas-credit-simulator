package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.command.SimulateLoanCommand
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import com.trendyol.kediatr.Command
import java.util.UUID

class StartBulkSimulationCommand(
  val bulkId: UUID = UUID.randomUUID(),
  val simulations: List<LoanSimulationCommandDto>
) : Command

data class LoanSimulationCommandDto(
  val loanAmount: Money,
  val customerInfo: CustomerInfo,
  val months: Int,
  val interestRate: Money,
  val sourceCurrency: String,
  val targetCurrency: String,
  val policyType: PolicyType,
) {
  companion object {

    fun BulkSimulationMessage.LoanSimulationMessage.toLoanSimulationCommandDto(policyType: PolicyType) =
      LoanSimulationCommandDto(
        loanAmount = this.loanAmount,
        customerInfo = this.customerInfo,
        months = this.months,
        interestRate = this.interestRate,
        sourceCurrency = this.sourceCurrency,
        targetCurrency = this.targetCurrency,
        policyType = policyType,
      )

    fun LoanSimulationCommandDto.toSimulateLoanCommand(interestRatePolicy: InterestRatePolicy) =
      SimulateLoanCommand(
        amount = this.loanAmount,
        customerInfo = this.customerInfo,
        termInMonths = Months(this.months),
        interestRatePolicy = interestRatePolicy,
        sourceCurrency = Currency(this.sourceCurrency),
        targetCurrency = Currency(this.targetCurrency),
      )
  }
}
