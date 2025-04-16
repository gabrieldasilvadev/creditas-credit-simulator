package br.com.simulator.credit.creditas.commondomain

interface DomainEventPublisher {
  fun publish(event: DomainEvent)

  fun publishAll(events: List<DomainEvent>)
}
