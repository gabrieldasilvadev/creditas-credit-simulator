package br.com.simulator.credit.creditas.persistence.adapter

import br.com.simulator.credit.creditas.persistence.documents.LoanSimulationDocument
import br.com.simulator.credit.creditas.persistence.repository.SimulationMongoRepository
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MongoSimulationPersistenceAdapterTest {

    private val repository = mockk<SimulationMongoRepository>(relaxed = true)
    private lateinit var adapter: MongoSimulationPersistenceAdapter

    @BeforeEach
    fun setup() {
        adapter = MongoSimulationPersistenceAdapter(repository)
    }

    @Test
    fun `should convert and save aggregate to Mongo`() {
        val aggregate = mockk<SimulateLoanAggregate>()
        val document = mockk<LoanSimulationDocument>()

        mockkObject(LoanSimulationDocument.Companion)
        every { LoanSimulationDocument.from(aggregate) } returns document
        every { repository.save(document) } returns document

        adapter.save(aggregate)

        verify { LoanSimulationDocument.from(aggregate) }
        verify { repository.save(document) }

        unmockkObject(LoanSimulationDocument.Companion)
    }
}
