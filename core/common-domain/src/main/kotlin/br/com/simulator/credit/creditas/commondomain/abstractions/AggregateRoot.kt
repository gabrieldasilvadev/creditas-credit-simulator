package br.com.simulator.credit.creditas.commondomain.abstractions

abstract class AggregateRoot<ID : Identifier<*>>(
  open val id: ID
)
