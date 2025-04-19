package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class LoanSimulationDataTest {
  @Test
  fun `should create LoanSimulationData using from factory`() {
    val loanAmount = Money(BigDecimal("10000.00"), Currency.BRL)
    val months = Months(12)
    val applicant =
      CustomerInfo(
        birthDate = LocalDate.of(1990, 1, 1),
        customerEmail = "cliente@teste.com",
      )
    val interestRate = Money(BigDecimal("0.03"), Currency.BRL)

    val simulationData =
      LoanSimulationData.from(
        loanAmount = loanAmount,
        duration = months,
        applicant = applicant,
        annualInterestRate = interestRate,
      )

    assertEquals(loanAmount, simulationData.loanAmount)
    assertEquals(months, simulationData.duration)
    assertEquals(applicant, simulationData.applicant)
    assertEquals(interestRate, simulationData.annualInterestRate)
  }
}
