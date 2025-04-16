package br.com.simulator.credit.creditas.commondomain

abstract class DomainObject {
  private val _domainEvents = mutableListOf<DomainEvent>()

  val domainEvents: List<DomainEvent>
    get() = _domainEvents

  fun registerEvent(event: DomainEvent) {
    _domainEvents.add(event)
  }

  private fun clearEvents() {
    _domainEvents.clear()
  }

  fun hasEvents(): Boolean {
    return _domainEvents.isNotEmpty()
  }

  fun getAndClearEvents(): List<DomainEvent> {
    val events = _domainEvents.toList()
    clearEvents()
    return events
  }

  open fun getAndClearEventsOfType(type: Class<out DomainEvent>): List<DomainEvent> {
    val events = _domainEvents.filter { it::class.java == type }
    _domainEvents.removeAll(events)
    return events
  }
}
