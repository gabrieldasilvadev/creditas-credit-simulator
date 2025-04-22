package br.com.simulator.credit.creditas.messaging.sqs.email

import br.com.simulator.credit.creditas.commondomain.ports.EmailSender
import br.com.simulator.credit.creditas.infrastructure.CorrelationInterceptor.Companion.CORRELATION_ID
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.notification.email.EmailContent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Monitorable
class EmailNotificationSqsListener(
  private val emailSender: EmailSender<EmailContent>,
  private val objectMapper: ObjectMapper,
) {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @Retryable(
    value = [Exception::class],
    maxAttempts = 4,
    backoff = Backoff(delay = 2000, multiplier = 2.0),
  )
  @SqsListener(value = ["\${cloud.aws.sqs.queues.emailNotificationQueue}"], acknowledgementMode = ON_SUCCESS)
  fun receive(
    @Payload rawMessage: String,
    @Headers messageHeaders: MessageHeaders,
  ) {
    val correlationId =
      (messageHeaders[CORRELATION_ID] as? String)
        ?: UUID.randomUUID().toString()
    MDC.put(CORRELATION_ID, correlationId)

    logger.info("Receiving message from SQS: $rawMessage")

    try {
      val event = objectMapper.readValue(rawMessage, SimulationCompletedEvent::class.java)
      emailSender.send(EmailContent.from(event))
      logger.info("Message successfully processed: $rawMessage")
    } finally {
      MDC.clear()
    }
  }
}
