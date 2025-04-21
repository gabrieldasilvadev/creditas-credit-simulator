package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.SimulationMongoRepository
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import java.util.Date
import java.util.Optional
import java.util.UUID

@Component
@Monitorable
class MongoSimulationPersistenceAdapter(
  private val simulationMongoRepository: SimulationMongoRepository,
  private val mongoTemplate: MongoTemplate,
) : SimulationPersistencePort {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override fun save(simulation: SimulateLoanAggregate) {
    logger.info("Saving simulation with distributed lock: $simulation")

    withSimulationLock(simulation.id.value) { _ ->
      val simulationDocument = LoanSimulationDocument.from(simulation)
      simulationMongoRepository.save(simulationDocument).also {
        logger.info("Simulation saved: $it")
      }
    } ?: run {
      logger.warn("Lock not acquired for simulationId=${simulation.id}. Skipping save.")
    }
  }

  fun tryLockSimulation(simulationId: UUID): Optional<LoanSimulationDocument> {
    logger.info("Trying to acquire distributed lock for simulationId: $simulationId")

    val query =
      Query(
        Criteria.where("_id").`is`(simulationId).and("locked").`is`(false),
      )

    val update =
      Update()
        .set("locked", true)
        .set("lockedAt", Date())

    val options = FindAndModifyOptions.options().returnNew(true)

    val lockedDoc = mongoTemplate.findAndModify(query, update, options, LoanSimulationDocument::class.java)

    return Optional.ofNullable(lockedDoc).also {
      if (it.isPresent) {
        logger.info("Lock acquired successfully for simulationId=$simulationId")
      } else {
        logger.warn("Lock already held by another instance for simulationId=$simulationId")
      }
    }
  }

  fun releaseLock(simulationId: UUID) {
    logger.info("Releasing lock for simulationId: $simulationId")
    val query = Query(Criteria.where("_id").`is`(simulationId))
    val update =
      Update()
        .set("locked", false)
        .unset("lockedAt")
    mongoTemplate.updateFirst(query, update, LoanSimulationDocument::class.java)
  }

  fun <T> withSimulationLock(
    simulationId: UUID,
    action: (LoanSimulationDocument) -> T,
  ): T? {
    val locked = tryLockSimulation(simulationId)
    return if (locked.isPresent) {
      try {
        action(locked.get())
      } finally {
        releaseLock(simulationId)
      }
    } else {
      null
    }
  }
}
