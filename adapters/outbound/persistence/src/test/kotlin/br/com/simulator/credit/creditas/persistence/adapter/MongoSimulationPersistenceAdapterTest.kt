package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.SimulationMongoRepository
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.UUID

internal class MongoSimulationPersistenceAdapterTest {
  private val repository = mockk<SimulationMongoRepository>()
  private val mongoTemplate = mockk<MongoTemplate>(relaxed = true)
  private lateinit var adapter: MongoSimulationPersistenceAdapter

  @BeforeEach
  fun setup() {
    clearAllMocks()
    adapter = spyk(MongoSimulationPersistenceAdapter(repository, mongoTemplate))
  }

  @Test
  fun `should insert new simulation document when not exists`() {
    val aggregate = mockk<SimulateLoanAggregate>()
    val simulationId = UUID.randomUUID()
    val document = mockk<LoanSimulationDocument>()

    every { aggregate.id.value } returns simulationId

    mockkObject(LoanSimulationDocument.Companion)
    every { LoanSimulationDocument.from(aggregate) } returns document
    every { repository.existsById(simulationId) } returns false
    every { repository.save(document) } returns document

    adapter.save(aggregate)

    verify(exactly = 1) { LoanSimulationDocument.from(aggregate) }
    verify(exactly = 1) { repository.existsById(simulationId) }
    verify(exactly = 1) { repository.save(document) }

    unmockkObject(LoanSimulationDocument.Companion)
  }

  @Test
  fun `should update simulation using distributed lock when already exists`() {
    val aggregate = mockk<SimulateLoanAggregate>()
    val document = mockk<LoanSimulationDocument>()
    val simulationId = UUID.randomUUID()

    every { aggregate.id.value } returns simulationId

    mockkObject(LoanSimulationDocument.Companion)
    every { LoanSimulationDocument.from(aggregate) } returns document
    every { repository.existsById(simulationId) } returns true

    every {
      mongoTemplate.findAndModify(any(), any(), any(), LoanSimulationDocument::class.java)
    } returns document

    every { repository.save(document) } returns document
    every { mongoTemplate.updateFirst(any(), any(), LoanSimulationDocument::class.java) } returns mockk()

    adapter.save(aggregate)

    verify {
      repository.existsById(simulationId)
      mongoTemplate.findAndModify(any(), any(), any(), LoanSimulationDocument::class.java)
      repository.save(document)
      mongoTemplate.updateFirst(any(), any(), LoanSimulationDocument::class.java)
    }

    unmockkObject(LoanSimulationDocument.Companion)
  }

  @Test
  fun `should not save when distributed lock is not acquired`() {
    val aggregate = mockk<SimulateLoanAggregate>()
    val document = mockk<LoanSimulationDocument>()
    val simulationId = UUID.randomUUID()

    every { aggregate.id.value } returns simulationId

    mockkObject(LoanSimulationDocument.Companion)
    every { LoanSimulationDocument.from(aggregate) } returns document
    every { repository.existsById(simulationId) } returns true

    every {
      mongoTemplate.findAndModify(any(), any(), any(), LoanSimulationDocument::class.java)
    } returns null

    adapter.save(aggregate)

    verify(exactly = 0) { repository.save(any()) }

    unmockkObject(LoanSimulationDocument.Companion)
  }
}
