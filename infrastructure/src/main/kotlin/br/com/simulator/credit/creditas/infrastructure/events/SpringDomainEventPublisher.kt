package br.com.simulator.credit.creditas.infrastructure.events

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent
import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringDomainEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) :
  DomainEventPublisher {
  private val logger = LoggerFactory.getLogger(SpringDomainEventPublisher::class.java)

  override fun publish(event: DomainEvent) {
    applicationEventPublisher.publishEvent(event)
  }

  override fun publishAll(events: List<DomainEvent>) {
    events.forEach { event ->
      applicationEventPublisher.publishEvent(event)
    }
  }
}
