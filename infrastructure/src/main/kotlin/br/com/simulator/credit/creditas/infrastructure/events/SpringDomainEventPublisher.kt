package br.com.simulator.credit.creditas.infrastructure.events

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent
import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEventPublisher
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
@Monitorable("SpringDomainEventPublisher")
class SpringDomainEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) :
  DomainEventPublisher {
  private val logger = LoggerFactory.getLogger(SpringDomainEventPublisher::class.java)

  override fun publish(event: DomainEvent) {
    logger.info("Publishing event: ${event.javaClass.simpleName}")
    applicationEventPublisher.publishEvent(event)
  }

  override fun publishAll(events: List<DomainEvent>) {
    events.forEach { event ->
      applicationEventPublisher.publishEvent(event)
    }
  }
}
