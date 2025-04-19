package br.com.simulator.credit.creditas.simulationdomain.service

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.Simulation
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult

object LoanCalculator {
  fun simulate(
    loanAmount: Money,
    months: Months,
    annualRate: Money,
  ): SimulationResult {
    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(loanAmount),
        months = months,
        annualRate = annualRate,
      )
    return simulation.calculate()
  }
}
