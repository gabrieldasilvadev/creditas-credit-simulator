package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@Monitorable("BulkSimulationPersistenceAdapter")
class BulkSimulationPersistenceAdapter(
  private val bulkSimulationMongoRepository: BulkSimulationMongoRepository,
) {
  fun save(bulkSimulationDocument: BulkSimulationDocument): BulkSimulationDocument {
    return bulkSimulationMongoRepository.save(bulkSimulationDocument)
  }

  fun findById(bulkId: UUID): Optional<BulkSimulationDocument> {
    return bulkSimulationMongoRepository.findById(bulkId)
  }
}
