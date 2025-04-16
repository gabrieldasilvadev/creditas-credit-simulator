package br.com.simulator.credit.creditas.commondomain

interface DomainEvent {
  val occurredOn: Long
  val aggregateId: String
}
