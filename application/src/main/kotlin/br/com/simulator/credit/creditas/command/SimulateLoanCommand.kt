package br.com.simulator.credit.creditas.command

import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import com.trendyol.kediatr.CommandWithResult

data class SimulateLoanCommand(
  val amount: Money,
  val customerInfo: CustomerInfo,
  val termInMonths: Months,
  val policyType: InterestRatePolicy,
  val sourceCurrency: Currency,
  val targetCurrency: Currency,
) : CommandWithResult<LoanSimulationHttpResponse>
