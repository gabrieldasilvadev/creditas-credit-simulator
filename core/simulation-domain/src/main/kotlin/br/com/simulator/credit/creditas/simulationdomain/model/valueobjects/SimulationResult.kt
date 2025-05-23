package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money

data class SimulationResult(
  val totalPayment: Money,
  val monthlyInstallment: Money,
  val totalInterest: Money,
)
