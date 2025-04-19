package br.com.simulator.credit.creditas.messaging.sqs

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.shared.messages.BulkSimulationMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Component
@Monitorable("SqsBulkSimulationQueuePublisherAdapter")
class SqsBulkSimulationQueuePublisherAdapter(
  private val sqsClient: SqsClient,
  private val objectMapper: ObjectMapper,
  @Value("\${cloud.aws.sqs.queues.bulkSimulationQueue}") private val queueUrl: String,
) {
  private val logger = LoggerFactory.getLogger(SqsBulkSimulationQueuePublisherAdapter::class.java)

  fun send(message: BulkSimulationMessage) {

    val messageBody = objectMapper.writeValueAsString(message)

    val request =
      SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(messageBody)
        .build()

    val response = sqsClient.sendMessage(request)
    logger.info("Message sent to SQS with ID: ${response.messageId()}")
  }
}
