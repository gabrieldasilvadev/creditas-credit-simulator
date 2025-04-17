package br.com.simulator.credit.creditas.commondomain.abstractions

interface DomainEvent {
  val occurredOn: Long
  val aggregateId: String
}
