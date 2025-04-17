package br.com.simulator.credit.creditas.commondomain.abstractions

interface DomainEventPublisher {
  fun publish(event: DomainEvent)

  fun publishAll(events: List<DomainEvent>)
}
