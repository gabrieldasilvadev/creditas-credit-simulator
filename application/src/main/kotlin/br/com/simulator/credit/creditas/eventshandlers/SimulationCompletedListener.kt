package br.com.simulator.credit.creditas.eventshandlers

import br.com.simulator.credit.creditas.messaging.sns.SnsEventPublisher
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SimulationCompletedListener(
  private val snsEventPublisher: SnsEventPublisher,
) {
  private val logger = LoggerFactory.getLogger(SimulationCompletedListener::class.java)

  @EventListener
  @Async
  fun handler(event: SimulationCompletedEvent) {
    println("[SimulationCompletedListener] E-MAIL received: $event")
    snsEventPublisher.publish(
      event,
      "arn:aws:sns:us-east-1:000000000000:credit-simulation-topic",
    )
    println("E-MAIL published: $event")
  }
}
