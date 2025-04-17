package br.com.simulator.credit.creditas.notification.email

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.api.events.LoanSimulationInputDataEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent
import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationResultEvent
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import org.junit.jupiter.api.Test
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.math.BigDecimal
import java.time.LocalDate

internal class SimulationResultEmailServiceTest {
  private val mailSender: JavaMailSender = mockk()
  private val service = SimulationResultEmailService(mailSender, "true")

  @Test
  fun `should send email with correct simulation result content`() {
    val applicant = CustomerInfo(LocalDate.of(1990, 1, 1), "user@example.com")
    val amount = Money(BigDecimal("10000.00"), Currency.BRL)
    val application = LoanSimulationData(amount, Months(12), applicant)
    val result =
      SimulationResult(
        totalPayment = Money("12000.00"),
        monthlyInstallment = Money("1000.00"),
        totalInterest = Money("2000.00"),
      )

    val event =
      SimulationCompletedEvent(
        LoanSimulationInputDataEvent.from(application),
        SimulationResultEvent.from(result),
        System.currentTimeMillis(),
        "12345",
      )
    val captor = slot<SimpleMailMessage>()

    every { mailSender.send(capture(captor)) } just runs

    service.send(
      EmailContent(
        to = applicant.customerEmail,
        subject = "Simulação de Empréstimo",
        body =
          """
          Simulação concluída:

          Valor solicitado: ${event.loanSimulationData.loanAmount.toView()}
          Prazo: ${event.loanSimulationData.months} meses
          Valor da parcela: ${event.result.monthlyInstallment.toView()}
          Total a pagar: ${event.result.totalPayment.toView()}
          Juros totais: ${event.result.totalInterest.toView()}
          """.trimIndent(),
      ),
    )

    val message = captor.captured

    assert(message.to!!.contains("user@example.com"))
    assert(message.subject!!.contains("Simulação"))
    assert(message.text!!.contains("Valor solicitado: ${amount.toView()}"))
    assert(message.text!!.contains("Prazo: 12 meses"))
    assert(message.text!!.contains("Valor da parcela: R$1000.00"))
    assert(message.text!!.contains("Total a pagar: R$12000.00"))
    assert(message.text!!.contains("Juros totais: R$2000.00"))
  }
}
