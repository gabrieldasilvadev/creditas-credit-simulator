package br.com.simulator.credit.creditas.commondomain.ports

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent

interface EventPublisher {
  fun publish(event: DomainEvent)
}
