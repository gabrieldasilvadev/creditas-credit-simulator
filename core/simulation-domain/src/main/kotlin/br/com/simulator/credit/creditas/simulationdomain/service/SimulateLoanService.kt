package br.com.simulator.credit.creditas.simulationdomain.service

import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import br.com.simulator.credit.creditas.simulationdomain.model.Simulation
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanAmount
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import java.util.UUID

class SimulateLoanService(
  private val interestRatePolicy: InterestRatePolicy,
) {
  fun execute(loanSimulationData: LoanSimulationData): Simulation {
    val annualRate = interestRatePolicy.annualInterestRate(loanSimulationData.applicant)

    val simulation =
      Simulation.create(
        loanAmount = LoanAmount(loanSimulationData.loanAmount),
        months = loanSimulationData.duration,
        annualRate = annualRate,
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

  companion object {
    fun of(policyType: InterestRatePolicy): SimulateLoanService {
      return SimulateLoanService(policyType)
    }
  }
}
