package br.com.simulator.credit.creditas.notification.email

import br.com.simulator.credit.creditas.simulationdomain.api.events.SimulationCompletedEvent

data class EmailContent(
  val to: String,
  val subject: String,
  val body: String,
  val contentType: ContentType = ContentType.PLAIN,
) {
  enum class ContentType {
    PLAIN,
    HTML,
  }

  companion object {
    fun from(event: SimulationCompletedEvent): EmailContent {
      return EmailContent(
        to = event.loanSimulationData.email,
        subject = "Simulation completed",
        body =
          """
          Simulação concluída:

          Valor solicitado: ${event.loanSimulationData.loanAmount.toView()}
          Prazo: ${event.loanSimulationData.months} meses
          Valor da parcela: ${event.result.monthlyInstallment.toView()}
          Total a pagar: ${event.result.totalPayment.toView()}
          Juros totais: ${event.result.totalInterest.toView()}
          """.trimIndent(),
      )
    }
  }
}
