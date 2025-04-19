package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.persistence.repository.BulkSimulationMongoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

internal class BulkSimulationPersistenceAdapterTest {
  private val mongoRepository = mockk<BulkSimulationMongoRepository>()
  private lateinit var adapter: BulkSimulationPersistenceAdapter

  @BeforeEach
  fun setup() {
    adapter = BulkSimulationPersistenceAdapter(mongoRepository)
  }

  @Test
  fun `should save document using repository`() {
    val document =
      BulkSimulationDocument(
        id = UUID.randomUUID(),
        processed = 0,
        results = listOf(),
        status = BulkSimulationStatus.PROCESSING,
        total = 0,
      )

    every { mongoRepository.save(document) } returns document

    val saved = adapter.save(document)

    verify(exactly = 1) { mongoRepository.save(document) }
    assert(saved == document)
  }

  @Test
  fun `should find document by id using repository`() {
    val bulkId = UUID.randomUUID()
    val document =
      BulkSimulationDocument(
        id = bulkId,
        processed = 0,
        results = listOf(),
        status = BulkSimulationStatus.PROCESSING,
        total = 0,
      )

    every { mongoRepository.findById(bulkId) } returns Optional.of(document)

    val found = adapter.findById(bulkId)

    verify(exactly = 1) { mongoRepository.findById(bulkId) }
    assert(found.isPresent)
    assert(found.get() == document)
  }
}
