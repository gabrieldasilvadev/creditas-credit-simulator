package br.com.simulator.credit.creditas.sqs

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class SqsBulkSimulationQueuePublisherAdapterTest {
  private val sqsClient = mockk<SqsClient>()
  private val objectMapper = mockk<ObjectMapper>()
  private val queueUrl = "https://sqs.us-east-1.amazonaws.com/000000000000/bulkSimulationQueue"

  private lateinit var adapter: SqsBulkSimulationQueuePublisherAdapter

  @BeforeEach
  fun setUp() {
    adapter = SqsBulkSimulationQueuePublisherAdapter(sqsClient, objectMapper, queueUrl)
  }

  @Test
  fun `should serialize message and send to SQS`() {
    val message =
      BulkSimulationMessage(
        bulkId = UUID.randomUUID(),
        simulations = emptyList(),
      )
    val serializedMessage = """{"bulkId":"${message.bulkId}","simulations":[]}"""

    val request =
      SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(serializedMessage)
        .build()

    every { objectMapper.writeValueAsString(message) } returns serializedMessage
    every { sqsClient.sendMessage(request) } returns SendMessageResponse.builder().messageId("abc-123").build()

    adapter.send(message)

    verify(exactly = 1) {
      sqsClient.sendMessage(request)
    }
  }

  @Test
  fun `should serialize message with simulations and send to SQS`() {
    val bulkId = UUID.randomUUID()
    val simulation = BulkSimulationMessage.LoanSimulationMessage(
      loanAmount = Money(BigDecimal("10000.00")),
      customerInfo = CustomerInfo(birthDate = LocalDate.of(1990, 1, 1), customerEmail = "john@example.com"),
      months = 12,
      interestRate = Money(BigDecimal("0.03")),
      sourceCurrency = "BRL",
      targetCurrency = "USD",
      policyType = PolicyType.FIXED
    )

    val message = BulkSimulationMessage(
      bulkId = bulkId,
      simulations = listOf(simulation)
    )

    val serializedMessage = """{"bulkId":"$bulkId","simulations":[{"loanAmount":{"amount":10000.00,"currency":"BRL"},"customerInfo":{"birthDate":"1990-01-01","customerEmail":"john@example.com"},"months":12,"interestRate":{"amount":0.03,"currency":"BRL"},"sourceCurrency":"BRL","targetCurrency":"USD","policyType":"FIXED"}]}"""

    val request =
      SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(serializedMessage)
        .build()

    every { objectMapper.writeValueAsString(message) } returns serializedMessage
    every { sqsClient.sendMessage(request) } returns SendMessageResponse.builder().messageId("abc-123").build()

    adapter.send(message)

    verify(exactly = 1) {
      sqsClient.sendMessage(request)
    }
  }

  @Test
  fun `should throw exception when serialization fails`() {
    val message = BulkSimulationMessage(
      bulkId = UUID.randomUUID(),
      simulations = emptyList()
    )

    val exception = mockk<JsonProcessingException>()
    every { objectMapper.writeValueAsString(message) } throws exception

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

    val request = SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(serializedMessage)
      .build()

    every { objectMapper.writeValueAsString(message) } returns serializedMessage
    every { sqsClient.sendMessage(request) } throws AwsServiceException.builder().message("SQS service error").build()

    assertThrows(AwsServiceException::class.java) {
      adapter.send(message)
    }
  }
}
