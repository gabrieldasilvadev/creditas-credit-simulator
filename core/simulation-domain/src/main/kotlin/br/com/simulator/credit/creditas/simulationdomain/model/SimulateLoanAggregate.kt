package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.abstractions.AggregateRoot
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult

class SimulateLoanAggregate(
  override val id: LoanSimulationId,
  val simulation: Simulation,
  val simulationResult: SimulationResult,
  val customerInfo: CustomerInfo,
) : AggregateRoot<LoanSimulationId>(id) {
  companion object {
    fun of(
      simulation: Simulation,
      customerInfo: CustomerInfo,
    ): SimulateLoanAggregate {
      val result = simulation.calculate()
      val id = LoanSimulationId()
      return SimulateLoanAggregate(id, simulation, result, customerInfo)
    }
  }
}
