package br.com.simulator.credit.creditas.persistence.repository

import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SimulationMongoRepository : MongoRepository<LoanSimulationDocument, UUID>
