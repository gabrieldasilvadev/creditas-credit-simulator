package br.com.simulator.credit.creditas.messaging.sns.notifications

import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo

data class SimulationNotification(
  val loanAmount: Money,
  val durationInMonths: Int,
  val monthlyInstallment: Money,
  val totalPayment: Money,
  val totalInterest: Money,
  val customerInfo: CustomerInfo,
)
