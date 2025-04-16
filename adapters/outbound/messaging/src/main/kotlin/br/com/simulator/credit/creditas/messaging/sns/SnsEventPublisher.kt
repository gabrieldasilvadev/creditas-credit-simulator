package br.com.simulator.credit.creditas.messaging.sns

import br.com.simulator.credit.creditas.commondomain.DomainEvent
import br.com.simulator.credit.creditas.commondomain.EventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class SnsEventPublisher(
  private val snsClient: SnsClient,
  private val objectMapper: ObjectMapper,
) : EventPublisher {
  @Retryable(
    value = [Exception::class],
    maxAttempts = 3,
    backoff = Backoff(delay = 1000, multiplier = 2.0),
  )
  override fun publish(
    event: DomainEvent,
    topicKey: String,
  ) {
    val message = objectMapper.writeValueAsString(event)

    val request =
      PublishRequest.builder()
        .topicArn("arn:aws:sns:us-east-1:000000000000:simulation-completed-topic")
        .message(message)
        .build()

    snsClient.publish(request)
  }

  @Recover
  fun recover(
    ex: Exception,
    event: Any,
    topicKey: String,
  ) {
    println("🔥 Failure to publish in the topic [$topicKey]. Fallback: $event")
  }
}
