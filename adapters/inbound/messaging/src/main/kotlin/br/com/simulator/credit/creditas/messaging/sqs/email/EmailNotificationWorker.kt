package br.com.simulator.credit.creditas.messaging.sqs.email

import br.com.simulator.credit.creditas.commondomain.ports.EmailSender
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.notification.email.EmailContent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS
import org.slf4j.LoggerFactory
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
@Monitorable("EmailNotificationWorker")
class EmailNotificationWorker(
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
    @Payload envelope: EmailEnvelope,
    @Headers messageHeaders: MessageHeaders,
  ) {
    logger.info("Received message: $envelope")
    val event = objectMapper.readValue(envelope.message, SimulationCompletedEvent::class.java)

    emailSender.send(EmailContent.from(event))
    logger.info("Message received: $event")
  }
}
