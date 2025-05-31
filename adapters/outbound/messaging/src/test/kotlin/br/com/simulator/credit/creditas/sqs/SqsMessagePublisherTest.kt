package br.com.simulator.credit.creditas.sqs

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import software.amazon.awssdk.services.sqs.model.SqsException
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class SqsMessagePublisherTest {
  private val sqsClient = mockk<SqsClient>()
  private val objectMapper = mockk<ObjectMapper>()
  private val queueUrl = "https://sqs.us-east-1.amazonaws.com/000000000000/bulkSimulationQueue"

  private lateinit var adapter: SqsBulkSimulationQueuePublisherAdapter

  @BeforeEach
  fun setUp() {
    adapter = SqsBulkSimulationQueuePublisherAdapter(sqsClient, objectMapper, queueUrl)
  }

  @Test
  fun `should serialize message with simulations and send to SQS`() {
    val bulkId = UUID.randomUUID()
    val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "test@example.com")

    val simulation = BulkSimulationMessage.LoanSimulationMessage(
      loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
      customerInfo = customerInfo,
      months = 12,
      interestRate = Money(BigDecimal("0.03"), Currency.BRL),
      sourceCurrency = "BRL",
      targetCurrency = "USD",
      policyType = PolicyType.FIXED
    )

    val message = BulkSimulationMessage(
      bulkId = bulkId,
      simulations = listOf(simulation)
    )

    val serializedMessage = """{"bulkId":"$bulkId","simulations":[{}]}"""

    val requestSlot = slot<SendMessageRequest>()

    every { objectMapper.writeValueAsString(message) } returns serializedMessage
    every { sqsClient.sendMessage(capture(requestSlot)) } returns
      SendMessageResponse.builder().messageId("abc-123").build()

    adapter.send(message)

    verify(exactly = 1) {
      objectMapper.writeValueAsString(message)
    }

    verify(exactly = 1) {
      sqsClient.sendMessage(any<SendMessageRequest>())
    }

    assert(requestSlot.captured.queueUrl() == queueUrl)
    assert(requestSlot.captured.messageBody() == serializedMessage)
  }

  @Test
  fun `should throw exception when serialization fails`() {
    val message = BulkSimulationMessage(
      bulkId = UUID.randomUUID(),
      simulations = emptyList()
    )

    val mockException = mockk<JsonProcessingException>(relaxed = true)
    every { objectMapper.writeValueAsString(message) } throws mockException

    assertThrows(JsonProcessingException::class.java) {
      adapter.send(message)
    }

    verify(exactly = 0) {
      sqsClient.sendMessage(any<SendMessageRequest>())
    }
  }

  @Test
  fun `should throw exception when SQS client fails`() {
    val message = BulkSimulationMessage(
      bulkId = UUID.randomUUID(),
      simulations = emptyList()
    )
    val serializedMessage = """{"bulkId":"${message.bulkId}","simulations":[]}"""

    val mockException = mockk<SqsException>(relaxed = true)

    every { objectMapper.writeValueAsString(message) } returns serializedMessage
    every { sqsClient.sendMessage(any<SendMessageRequest>()) } throws mockException

    assertThrows(SqsException::class.java) {
      adapter.send(message)
    }
  }
}
