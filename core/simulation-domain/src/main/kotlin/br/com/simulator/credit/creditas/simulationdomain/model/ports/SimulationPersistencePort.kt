package br.com.simulator.credit.creditas.simulationdomain.model.ports

import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate

interface SimulationPersistencePort {
  fun save(simulation: SimulateLoanAggregate)
}
