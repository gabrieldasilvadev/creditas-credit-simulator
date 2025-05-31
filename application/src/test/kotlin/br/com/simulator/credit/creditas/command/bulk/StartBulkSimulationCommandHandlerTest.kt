package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class StartBulkSimulationCommandHandlerTest {
  private val repository = mockk<BulkSimulationPersistenceAdapter>()
  private val publisher = mockk<SqsBulkSimulationQueuePublisherAdapter>(relaxed = true)
  private val objectMapper = mockk<ObjectMapper>(relaxed = true)

  private lateinit var handler: StartBulkSimulationCommandHandler

  @BeforeEach
  fun setup() {
    handler = StartBulkSimulationCommandHandler(repository, publisher, objectMapper)
  }

  @Test
  fun `should persist document and publish simulation message`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")

      val dto =
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 12,
          interestRate = Money(BigDecimal("0.03")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = PolicyType.AGE_BASED,
        )

      val command =
        StartBulkSimulationCommand(
          bulkId = bulkId,
          simulations = listOf(dto),
        )

      every {
        repository.save(
          match {
            it.id == bulkId &&
              it.status == BulkSimulationStatus.PROCESSING &&
              it.processed == 0 &&
              it.total == 1
          },
        )
      } returns
        BulkSimulationDocument(
          id = bulkId,
          status = BulkSimulationStatus.PROCESSING,
          processed = 0,
          total = 1,
        )

      handler.handle(command)

      verify(exactly = 1) {
        repository.save(any())
      }

      verify(exactly = 1) {
        publisher.send(
          match {
            it.bulkId == bulkId &&
              it.simulations.size == 1 &&
              it.simulations[0].loanAmount.amount == BigDecimal("10000.00") &&
              it.simulations[0].months == 12 &&
              it.simulations[0].sourceCurrency == "BRL" &&
              it.simulations[0].targetCurrency == "USD" &&
              it.simulations[0].policyType == PolicyType.AGE_BASED
          },
        )
      }
    }

  @Test
  fun `should handle multiple simulations in a batch`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")

      val dto1 =
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 12,
          interestRate = Money(BigDecimal("0.03")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = PolicyType.AGE_BASED,
        )

      val dto2 =
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("20000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 24,
          interestRate = Money(BigDecimal("0.05")),
          sourceCurrency = "BRL",
          targetCurrency = "EUR",
          policyType = PolicyType.FIXED,
        )

      val command =
        StartBulkSimulationCommand(
          bulkId = bulkId,
          simulations = listOf(dto1, dto2),
        )

      every {
        repository.save(
          match {
            it.id == bulkId &&
              it.status == BulkSimulationStatus.PROCESSING &&
              it.processed == 0 &&
              it.total == 2
          },
        )
      } returns
        BulkSimulationDocument(
          id = bulkId,
          status = BulkSimulationStatus.PROCESSING,
          processed = 0,
          total = 2,
        )

      every { objectMapper.writeValueAsBytes(any()) } returns ByteArray(1000)

      handler.handle(command)

      verify(exactly = 1) {
        repository.save(any())
      }

      verify(exactly = 1) {
        publisher.send(
          match {
            it.bulkId == bulkId &&
              it.simulations.size == 2 &&
              it.simulations[0].loanAmount.amount == BigDecimal("10000.00") &&
              it.simulations[1].loanAmount.amount == BigDecimal("20000.00")
          },
        )
      }
    }

  @Test
  fun `should split batch when payload is too large`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")

      val simulations = (1..10).map { i ->
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("${i}0000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 12 * i,
          interestRate = Money(BigDecimal("0.0${i}")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = if (i % 2 == 0) PolicyType.AGE_BASED else PolicyType.FIXED,
        )
      }

      val command =
        StartBulkSimulationCommand(
          bulkId = bulkId,
          simulations = simulations,
        )

      every {
        repository.save(any())
      } returns
        BulkSimulationDocument(
          id = bulkId,
          status = BulkSimulationStatus.PROCESSING,
          processed = 0,
          total = 10,
        )

      val messageSlot = slot<BulkSimulationMessage>()
      var firstCall = true

      every { objectMapper.writeValueAsBytes(capture(messageSlot)) } answers {
        if (firstCall && messageSlot.captured.simulations.size > 5) {
          firstCall = false
          ByteArray(300 * 1024)
        } else {
          ByteArray(1000)
        }
      }

      handler.handle(command)

      verify(exactly = 1) {
        repository.save(any())
      }

      verify(atLeast = 2) {
        publisher.send(any())
      }
    }

  @Test
  fun `should throw exception when single simulation payload is too large`() =
    runBlocking {
      val bulkId = UUID.randomUUID()
      val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "cliente@teste.com")

      val dto =
        LoanSimulationCommandDto(
          loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
          customerInfo = customerInfo,
          months = 12,
          interestRate = Money(BigDecimal("0.03")),
          sourceCurrency = "BRL",
          targetCurrency = "USD",
          policyType = PolicyType.AGE_BASED,
        )

      val command =
        StartBulkSimulationCommand(
          bulkId = bulkId,
          simulations = listOf(dto),
        )

      every {
        repository.save(any())
      } returns
        BulkSimulationDocument(
          id = bulkId,
          status = BulkSimulationStatus.PROCESSING,
          processed = 0,
          total = 1,
        )

      every { objectMapper.writeValueAsBytes(any()) } returns ByteArray(300 * 1024)

      val exception = assertThrows(IllegalStateException::class.java) {
        runBlocking {
          handler.handle(command)
        }
      }

      assertEquals("Cannot send single simulation; payload too large", exception.message)

      verify(exactly = 1) {
        repository.save(any())
      }

      verify(exactly = 0) {
        publisher.send(any())
      }
    }
}
