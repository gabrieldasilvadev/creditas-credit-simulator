package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationResponseDto
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
import java.util.Date
import java.util.Optional
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
@Monitorable
class BulkSimulationPersistenceAdapter(
  private val bulkSimulationMongoRepository: BulkSimulationMongoRepository,
  private val mongoTemplate: MongoTemplate,
) {
  private val logger = LoggerFactory.getLogger(BulkSimulationPersistenceAdapter::class.java)

  fun save(bulkSimulationDocument: BulkSimulationDocument): BulkSimulationDocument? {
    val bulkId = bulkSimulationDocument.id
    if (!bulkSimulationMongoRepository.existsById(bulkId)) {
      logger.info("Inserting new bulk simulation document: $bulkSimulationDocument")
      return bulkSimulationMongoRepository.save(bulkSimulationDocument)
        .also { logger.info("Initial bulk simulation inserted: $it") }
    }
    logger.info("Updating bulk simulation under distributed lock: id=$bulkId")
    val result = withMongoLock(bulkId) { _ ->
      bulkSimulationMongoRepository.save(bulkSimulationDocument)
        .also { logger.info("Bulk simulation document saved under lock: $it") }
    }
    if (result == null) {
      logger.warn("Could not acquire lock for bulkId=$bulkId. Skipping save.")
    }
    return result
  }

  fun updateIncrementAndPushResult(bulkId: UUID, dto: BulkSimulationResponseDto, isLast: Boolean) {
    val query = Query(Criteria.where("_id").`is`(bulkId))
    val update = Update()
      .inc("processed", 1)
      .push("results", dto)
      .set("status", if (isLast) BulkSimulationStatus.COMPLETED else BulkSimulationStatus.PROCESSING)
    mongoTemplate.updateFirst(query, update, BulkSimulationDocument::class.java)
    logger.debug("Incremented processed and pushed result for bulkId=$bulkId (last=$isLast)")
  }

  fun updateStatus(bulkId: UUID, status: BulkSimulationStatus) {
    val query = Query(Criteria.where("_id").`is`(bulkId))
    val update = Update().set("status", status)
    mongoTemplate.updateFirst(query, update, BulkSimulationDocument::class.java)
    logger.debug("Updated status for bulkId=$bulkId to $status")
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
    logger.info("Trying to acquire lock for bulkId=$bulkId")
    val query = Query(
      Criteria.where("_id").`is`(bulkId)
        .and("locked").`is`(false)
    )
    val update = Update()
      .set("locked", true)
      .set("lockedAt", Date())
    val options = FindAndModifyOptions.options().returnNew(true)
    val lockedDoc = mongoTemplate.findAndModify(
      query, update, options, BulkSimulationDocument::class.java
    )
    return Optional.ofNullable(lockedDoc).also {
      if (it.isPresent) {
        logger.info("Lock successfully acquired for bulkId=$bulkId")
      } else {
        logger.warn("Lock already held by another process for bulkId=$bulkId")
      }
    }
  }

  fun releaseLock(bulkId: UUID) {
    logger.info("Releasing lock for bulkId=$bulkId")
    val query = Query(Criteria.where("_id").`is`(bulkId))
    val update = Update()
      .set("locked", false)
      .unset("lockedAt")
    mongoTemplate.updateFirst(query, update, BulkSimulationDocument::class.java)
  }

  private fun <T> withMongoLock(
    bulkId: UUID,
    action: (BulkSimulationDocument) -> T
  ): T? {
    val lockedOpt = tryLockById(bulkId)
    return if (lockedOpt.isPresent) {
      try {
        action(lockedOpt.get())
      } finally {
        releaseLock(bulkId)
      }
    } else {
      null
    }
  }
}
