package br.com.simulator.credit.creditas.eventshandlers

import br.com.simulator.credit.creditas.messaging.sns.SnsEventPublisher
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SimulationCompletedListener(
  private val snsEventPublisher: SnsEventPublisher,
  @Value("\${cloud.aws.sns.topics.emailNotificationTopic}")
  private val topic: String,
) {
  private val logger = LoggerFactory.getLogger(SimulationCompletedListener::class.java)

  @EventListener
  fun handler(event: SimulationCompletedEvent) {
    logger.info("[SimulationCompletedListener] E-MAIL received: $event")
    snsEventPublisher.publish(event, topic)
    logger.info("E-MAIL published: $event")
  }
}
