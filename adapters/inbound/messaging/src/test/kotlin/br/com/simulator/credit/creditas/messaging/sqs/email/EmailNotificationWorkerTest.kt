package br.com.simulator.credit.creditas.messaging.sqs.email

import br.com.simulator.credit.creditas.commondomain.ports.EmailSender
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.notification.email.EmailContent
import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.messaging.MessageHeaders
import java.math.BigDecimal

internal class EmailNotificationWorkerTest {
  private val emailSender = mockk<EmailSender<EmailContent>>(relaxed = true)
  private val objectMapper = mockk<ObjectMapper>()

  private lateinit var worker: EmailNotificationWorker

  @BeforeEach
  fun setup() {
    worker = EmailNotificationWorker(emailSender, objectMapper)
  }

  @Test
  fun `should deserialize SimulationCompletedEvent and send email`() {
    val email = "cliente@teste.com"
    val jsonMessage = """{ "fake": "json" }"""
    val envelope =
      EmailEnvelope(
        type = "Notification",
        messageId = "123",
        message = jsonMessage,
        timestamp = "2024-01-01T00:00:00Z",
      )
    val headers = MessageHeaders(emptyMap())

    val simulationEvent =
      SimulationCompletedEvent(
        loanSimulationData =
          LoanSimulationInputDataEvent(
            email = email,
            birthDate = "1990-01-01",
            loanAmount = Money(BigDecimal("10000.00")),
            currency = "BRL",
            months = 12,
          ),
        result =
          SimulationResultEvent(
            totalPayment = Money(BigDecimal("12000.00")),
            monthlyInstallment = Money(BigDecimal("1000.00")),
            totalInterest = Money(BigDecimal("2000.00")),
          ),
        occurredOn = System.currentTimeMillis(),
        aggregateId = "sim-123",
      )

    val slot = slot<EmailContent>()

    every { objectMapper.readValue(jsonMessage, SimulationCompletedEvent::class.java) } returns simulationEvent
    every { emailSender.send(capture(slot)) } just Runs

    worker.receive(envelope, headers)

    verify(exactly = 1) { emailSender.send(any()) }

    val sentEmail = slot.captured
    assert(sentEmail.to == email)
    assert(sentEmail.subject.contains("Simulation completed"))
    assert(sentEmail.body.contains("R$10000.00"))
    assert(sentEmail.body.contains("R$12000.00"))
    assert(sentEmail.body.contains("R$2000.00"))
  }
}
