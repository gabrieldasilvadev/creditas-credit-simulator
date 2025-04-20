package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
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
@Monitorable("BulkSimulationPersistenceAdapter")
class BulkSimulationPersistenceAdapter(
  private val bulkSimulationMongoRepository: BulkSimulationMongoRepository,
  private val mongoTemplate: MongoTemplate,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  fun save(bulkSimulationDocument: BulkSimulationDocument): BulkSimulationDocument {
    logger.info("Saving bulk simulation document: $bulkSimulationDocument")
    return bulkSimulationMongoRepository.save(bulkSimulationDocument).also {
      logger.info("Bulk simulation document saved: $it")
    }
  }

  fun findById(bulkId: UUID): Optional<BulkSimulationDocument> {
    logger.info("Finding bulk simulation document by ID: $bulkId")
    return bulkSimulationMongoRepository.findById(bulkId).also {
      if (it.isPresent) {
        logger.info("Bulk simulation document found: ${it.get()}")
      } else {
        logger.warn("No bulk simulation document found for ID: $bulkId")
      }
    }
  }

  fun tryLockById(bulkId: UUID): Optional<BulkSimulationDocument> {
    logger.info("Trying to purchase lock for the bulkid: $bulkId")

    val query =
      Query(
        Criteria.where("_id").`is`(bulkId).and("locked").`is`(false),
      )

    val update =
      Update()
        .set("locked", true)
        .set("lockedAt", Date())

    val options = FindAndModifyOptions.options().returnNew(true)

    val lockedDoc = mongoTemplate.findAndModify(query, update, options, BulkSimulationDocument::class.java)

    return Optional.ofNullable(lockedDoc).also {
      if (it.isPresent) {
        logger.info("Lock Successfully acquired for bulkid=$bulkId")
      } else {
        logger.warn("Lock is already in use for bulkid=$bulkId")
      }
    }
  }

  fun releaseLock(bulkId: UUID) {
    logger.info("Releasing Bulkid lock: $bulkId")

    val query = Query(Criteria.where("_id").`is`(bulkId))

    val update =
      Update()
        .set("locked", false)
        .unset("lockedAt")

    mongoTemplate.updateFirst(query, update, BulkSimulationDocument::class.java)
  }

  fun <T> withMongoLock(
    bulkId: UUID,
    action: (BulkSimulationDocument) -> T,
  ): T? {
    val locked = tryLockById(bulkId)
    return if (locked.isPresent) {
      try {
        action(locked.get())
      } finally {
        releaseLock(bulkId)
      }
    } else {
      null
    }
  }
}
