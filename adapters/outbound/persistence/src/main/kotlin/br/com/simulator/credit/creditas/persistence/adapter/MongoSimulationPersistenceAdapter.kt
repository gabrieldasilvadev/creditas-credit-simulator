package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.SimulationMongoRepository
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import org.springframework.stereotype.Component

@Component

@Monitorable("MongoSimulationPersistenceAdapter")
class MongoSimulationPersistenceAdapter(
  private val simulationMongoRepository: SimulationMongoRepository,
) : SimulationPersistencePort {
  override fun save(simulation: SimulateLoanAggregate) {
    val simulationDocument = LoanSimulationDocument.from(simulation)
    simulationMongoRepository.save(simulationDocument)
  }
}
