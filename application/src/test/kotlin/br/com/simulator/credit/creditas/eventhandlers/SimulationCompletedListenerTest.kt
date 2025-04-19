package br.com.simulator.credit.creditas.eventhandlers

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.eventshandlers.SimulationCompletedListener
import br.com.simulator.credit.creditas.messaging.sns.SnsEventPublisher
import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

internal class SimulationCompletedListenerTest {
  private val snsPublisher = mockk<SnsEventPublisher>(relaxed = true)
  private val topic = "arn:aws:sns:us-east-1:000000000000:emailNotificationTopic"

  private lateinit var listener: SimulationCompletedListener

  @BeforeEach
  fun setUp() {
    listener = SimulationCompletedListener(snsPublisher, topic)
  }

  @Test
  fun `should publish event to SNS when received`() {
    val event =
      SimulationCompletedEvent(
        loanSimulationData =
          LoanSimulationInputDataEvent(
            email = "cliente@teste.com",
            birthDate = "1990-01-01",
            loanAmount = Money(BigDecimal("10000.00")),
            currency = "USD",
            months = 12,
          ),
        result =
          SimulationResultEvent(
            totalPayment = Money(BigDecimal("12000.00")),
            monthlyInstallment = Money(BigDecimal("1000.00")),
            totalInterest = Money(BigDecimal("2000.00")),
          ),
        occurredOn = System.currentTimeMillis(),
        aggregateId = UUID.randomUUID().toString(),
      )

    listener.handler(event)

    verify(exactly = 1) {
      snsPublisher.publish(event, topic)
    }
  }
}
