package br.com.simulator.credit.creditas.messaging.sns

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent
import br.com.simulator.credit.creditas.commondomain.ports.EventPublisher
import br.com.simulator.credit.creditas.infrastructure.CorrelationInterceptor.Companion.CORRELATION_ID
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.MDC
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import java.util.UUID

@Component
@Monitorable
class SnsEventPublisher(
  private val snsClient: SnsClient,
  private val objectMapper: ObjectMapper,
) : EventPublisher {
  private val logger = org.slf4j.LoggerFactory.getLogger(SnsEventPublisher::class.java)

  @Retryable(
    value = [Exception::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1000, multiplier = 2.0),
  )
  override fun publish(
    event: DomainEvent,
    topic: String,
  ) {
    logger.info("Publishing sns event: $event")

    val correlationId = MDC.get(CORRELATION_ID) ?: UUID.randomUUID().toString()
    val message = objectMapper.writeValueAsString(event)

    val request =
      PublishRequest.builder()
        .topicArn(topic)
        .message(message)
        .messageAttributes(
          mapOf(
            CORRELATION_ID to
              MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(correlationId)
                .build(),
          ),
        )
        .build()

    logger.info("Publishing sns event: $request")

    snsClient.publish(request).also {
      logger.info("Message published to SNS: $it")
    }
  }

  @Recover
  fun recover(
    ex: Exception,
    event: Any,
    topicKey: String,
  ) {
    logger.info("Failure to publish in the topic [$topicKey]. Fallback: $event")
  }
}
