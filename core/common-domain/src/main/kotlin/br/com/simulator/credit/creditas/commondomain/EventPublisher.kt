package br.com.simulator.credit.creditas.commondomain

interface EventPublisher {
  fun publish(
    event: DomainEvent,
    topicKey: String,
  )
}
