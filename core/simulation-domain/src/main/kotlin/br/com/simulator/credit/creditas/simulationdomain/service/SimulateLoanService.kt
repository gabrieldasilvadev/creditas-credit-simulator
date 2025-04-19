package br.com.simulator.credit.creditas.simulationdomain.service

import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import br.com.simulator.credit.creditas.simulationdomain.model.Simulation
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import java.util.UUID

class SimulateLoanService {
  companion object {
    fun execute(loanSimulationData: LoanSimulationData): Simulation {
      val simulation =
        Simulation.create(
          loanAmount = LoanAmount(loanSimulationData.loanAmount),
          months = loanSimulationData.duration,
          annualRate = loanSimulationData.annualInterestRate,
        )

      val simulationResult = simulation.calculate()

      simulation.registerEvent(
        SimulationCompletedEvent(
          loanSimulationData = LoanSimulationInputDataEvent.from(loanSimulationData),
          result = SimulationResultEvent.from(simulationResult),
          occurredOn = System.currentTimeMillis(),
          aggregateId = UUID.randomUUID().toString(),
        ),
      )
      return simulation
    }
  }
}
