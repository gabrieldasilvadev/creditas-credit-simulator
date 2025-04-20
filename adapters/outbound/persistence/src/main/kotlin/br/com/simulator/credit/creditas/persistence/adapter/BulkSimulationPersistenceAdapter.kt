package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
import java.util.Optional
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable("BulkSimulationPersistenceAdapter")
class BulkSimulationPersistenceAdapter(
  private val bulkSimulationMongoRepository: BulkSimulationMongoRepository,
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
}
