package br.com.simulator.credit.creditas.infrastructure

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent
import br.com.simulator.credit.creditas.infrastructure.events.SpringDomainEventPublisher
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

internal class SpringDomainEventPublisherTest {

  private val springPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
  private lateinit var domainPublisher: SpringDomainEventPublisher

  data class DummyEvent(val name: String) : DomainEvent {
    override val aggregateId = "123"
    override val occurredOn = System.currentTimeMillis()
  }

  @BeforeEach
  fun setUp() {
    domainPublisher = SpringDomainEventPublisher(springPublisher)
  }

  @Test
  fun `should publish single event`() {
    val event = DummyEvent("one")

    domainPublisher.publish(event)

    verify(exactly = 1) {
      springPublisher.publishEvent(event)
    }
  }

  @Test
  fun `should publish all events`() {
    val events = listOf(
      DummyEvent("one"),
      DummyEvent("two"),
      DummyEvent("three")
    )

    domainPublisher.publishAll(events)

    verify(exactly = 3) {
      springPublisher.publishEvent(any<DomainEvent>())
    }
  }
}
