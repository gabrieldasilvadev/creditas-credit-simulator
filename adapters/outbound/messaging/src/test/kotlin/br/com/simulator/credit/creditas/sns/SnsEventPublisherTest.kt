package br.com.simulator.credit.creditas.sns

import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEvent
import br.com.simulator.credit.creditas.messaging.sns.SnsEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

internal class SnsEventPublisherTest {
  private val snsClient = mockk<SnsClient>(relaxed = true)
  private val objectMapper = mockk<ObjectMapper>()

  private lateinit var publisher: SnsEventPublisher

  data class DummyEvent(
    val id: String,
    val type: String,
  ) : DomainEvent {
    override val aggregateId = id
    override val occurredOn = System.currentTimeMillis()
  }

  @BeforeEach
  fun setUp() {
    publisher = SnsEventPublisher(snsClient, objectMapper)
  }

  @Test
  fun `should serialize event and publish to SNS`() {
    val event = DummyEvent("123", "SIMULATION_COMPLETED")
    val serialized = """{"id":"123","type":"SIMULATION_COMPLETED"}"""
    val slot = slot<PublishRequest>()

    every { objectMapper.writeValueAsString(event) } returns serialized
    every { snsClient.publish(capture(slot)) } returns mockk()

    publisher.publish(event, "simulation-completed-topic")

    verify(exactly = 1) {
      snsClient.publish(any<PublishRequest>())
    }

    val capturedRequest = slot.captured
    println("Captured message: ${capturedRequest.message()}")
    println("Captured topicArn: ${capturedRequest.topicArn()}")

    assert(capturedRequest.message().contains("123"))
    assert(capturedRequest.topicArn().endsWith("simulation-completed-topic"))
  }

  @Test
  fun `should call fallback when exception is thrown`() {
    val event = DummyEvent("999", "ERROR_EVENT")
    publisher.recover(RuntimeException("fail"), event, "simulation-completed-topic")
  }
}
