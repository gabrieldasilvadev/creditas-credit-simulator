package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.Optional
import java.util.UUID

internal class BulkSimulationPersistenceAdapterTest {
  private val mongoRepository = mockk<BulkSimulationMongoRepository>()
  private val mongoTemplate = mockk<MongoTemplate>(relaxed = true)
  private lateinit var adapter: BulkSimulationPersistenceAdapter

  @BeforeEach
  fun setup() {
    clearAllMocks()
    adapter = spyk(BulkSimulationPersistenceAdapter(mongoRepository, mongoTemplate))
  }

  @Test
  fun `should insert document if it does not exist`() {
    val document = buildDocument()
    every { mongoRepository.existsById(document.id) } returns false
    every { mongoRepository.save(document) } returns document

    val saved = adapter.save(document)

    verifyOrder {
      mongoRepository.existsById(document.id)
      mongoRepository.save(document)
    }

    assert(saved == document)
  }

  @Test
  fun `should update document using distributed lock if it already exists`() {
    val document = buildDocument()
    every { mongoRepository.existsById(document.id) } returns true
    val lockedDoc = document.copy(locked = false)

    every {
      mongoTemplate.findAndModify(
        any(),
        any(),
        any(),
        BulkSimulationDocument::class.java,
      )
    } returns lockedDoc

    every { mongoTemplate.updateFirst(any(), any(), BulkSimulationDocument::class.java) } returns mockk()
    every { mongoRepository.save(document) } returns document

    val saved = adapter.save(document)

    verify {
      mongoRepository.existsById(document.id)
      mongoTemplate.findAndModify(any(), any(), any(), BulkSimulationDocument::class.java)
      mongoRepository.save(document)
      mongoTemplate.updateFirst(any(), any(), BulkSimulationDocument::class.java)
    }

    assert(saved == document)
  }

  @Test
  fun `should return empty when lock is not acquired`() {
    val document = buildDocument()
    every { mongoRepository.existsById(document.id) } returns true

    every {
      mongoTemplate.findAndModify(
        any(),
        any(),
        any(),
        BulkSimulationDocument::class.java,
      )
    } returns null

    val result = adapter.save(document)

    verify(exactly = 0) { mongoRepository.save(document) }
    assert(result == null)
  }

  @Test
  fun `should find document by id using repository`() {
    val document = buildDocument()
    every { mongoRepository.findById(document.id) } returns Optional.of(document)

    val found = adapter.findById(document.id)

    verify { mongoRepository.findById(document.id) }
    assert(found.isPresent)
    assert(found.get() == document)
  }

  private fun buildDocument(): BulkSimulationDocument =
    BulkSimulationDocument(
      id = UUID.randomUUID(),
      processed = 0,
      results = listOf(),
      status = BulkSimulationStatus.PROCESSING,
      total = 0,
    )
}
