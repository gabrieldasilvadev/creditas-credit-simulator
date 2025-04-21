package br.com.simulator.credit.creditas.notification.email

import br.com.simulator.credit.creditas.commondomain.ports.EmailSender
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@Monitorable
class SimulationResultEmailService(
  private val emailSender: JavaMailSender,
  @Value("\${feature.toggle.send-email}")
  private val sendEmailToggle: String,
) : EmailSender<EmailContent> {
  private val logger = LoggerFactory.getLogger(SimulationResultEmailService::class.java)

  override fun send(content: EmailContent) {
    val message =
      SimpleMailMessage().apply {
        from = "noreply@creditas.com"
        setTo(content.to)
        subject = content.subject
        text = content.body
      }

    if (sendEmailToggle.toBoolean()) {
      emailSender.send(message)
      logger.info("Email sent to ${content.to} with the subject: ${content.subject}")
      return
    }

    logger.info("Feature toggle is off. Email not sent to ${content.to} with the subject: ${content.subject}")
  }
}
