package br.com.simulator.credit.creditas.sqs

import br.com.simulator.credit.creditas.messaging.sqs.SqsBulkSimulationQueuePublisherAdapter
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
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
}
