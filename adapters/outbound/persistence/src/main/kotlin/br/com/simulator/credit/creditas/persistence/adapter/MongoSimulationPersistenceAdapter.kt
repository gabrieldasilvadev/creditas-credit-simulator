package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.SimulationMongoRepository
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable("MongoSimulationPersistenceAdapter")
class MongoSimulationPersistenceAdapter(
  private val simulationMongoRepository: SimulationMongoRepository,
) : SimulationPersistencePort {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override fun save(simulation: SimulateLoanAggregate) {
    logger.info("Saving simulation: $simulation")
    val simulationDocument = LoanSimulationDocument.from(simulation)
    simulationMongoRepository.save(simulationDocument).also {
      logger.info("Simulation saved: $it")
    }
  }
}
