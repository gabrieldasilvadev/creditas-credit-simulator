package br.com.simulator.credit.creditas.shared.messages

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import java.time.LocalDateTime
import java.util.UUID

data class BulkSimulationMessage(
  val bulkId: UUID,
  val simulations: List<LoanSimulationMessage>,
  val requestedAt: LocalDateTime = LocalDateTime.now(),
) {
  data class LoanSimulationMessage(
    val loanAmount: Money,
    val customerInfo: CustomerInfo,
    val months: Int,
    val interestRate: Money,
    val sourceCurrency: String,
    val targetCurrency: String,
    val policyType: PolicyType,
  )
}
