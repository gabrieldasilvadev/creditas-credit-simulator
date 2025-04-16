package br.com.simulator.credit.creditas.persistence.repository

import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BulkSimulationMongoRepository : MongoRepository<BulkSimulationDocument, UUID>
