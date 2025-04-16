package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult

class SimulateLoanAggregate(
  val simulation: Simulation,
  val simulationResult: SimulationResult,
  val customerInfo: CustomerInfo,
) {
  companion object {
    fun of(
      simulation: Simulation,
      customerInfo: CustomerInfo,
    ): SimulateLoanAggregate {
      val result = simulation.calculate()
      return SimulateLoanAggregate(simulation, result, customerInfo)
    }
  }
}
