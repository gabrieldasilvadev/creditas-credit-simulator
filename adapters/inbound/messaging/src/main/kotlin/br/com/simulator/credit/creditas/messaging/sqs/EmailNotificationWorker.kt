package br.com.simulator.credit.creditas.messaging.sqs

import br.com.simulator.credit.creditas.commondomain.ports.EmailSender
import br.com.simulator.credit.creditas.notification.email.EmailContent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode.ON_SUCCESS
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class EmailNotificationWorker(
  private val emailSender: EmailSender<EmailContent>,
  private val objectMapper: ObjectMapper,
) {
  @Retryable(
    value = [Exception::class],
    maxAttempts = 4,
    backoff = Backoff(delay = 2000, multiplier = 2.0),
  )
  @SqsListener(value = ["email-notification-queue"], acknowledgementMode = ON_SUCCESS)
  fun receive(
    @Payload envelope: EmailEnvelope,
    @Headers messageHeaders: MessageHeaders,
  ) {
    val event = objectMapper.readValue(envelope.message, SimulationCompletedEvent::class.java)

    emailSender.send(EmailContent.from(event))
    println("Message received: $event")
  }
}
