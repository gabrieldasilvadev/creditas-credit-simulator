package br.com.simulator.credit.creditas.persistence.documents

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = "simulation")
data class LoanSimulationDocument(
  @Id
  val id: UUID? = null,
  val email: String,
  val birthDate: LocalDate,
  val loanAmount: BigDecimal,
  val currency: String,
  val months: Int,
  val monthlyInstallment: BigDecimal,
  val totalPayment: BigDecimal,
  val totalInterest: BigDecimal,
  val monthlyRate: BigDecimal,
  var locked: Boolean = false,
  @field:CreatedDate
  val createdAt: LocalDateTime? = null,
  val createdBy: UUID? = null,
) {
  fun toAggregate(): SimulationResult {
    return SimulationResult(
      totalPayment = Money(this.totalPayment, Currency(this.currency)),
      monthlyInstallment = Money(this.monthlyInstallment, Currency(this.currency)),
      totalInterest = Money(this.totalInterest, Currency(this.currency)),
    )
  }

  companion object {
    fun from(simulateLoanAggregate: SimulateLoanAggregate) =
      LoanSimulationDocument(
        id = simulateLoanAggregate.id.value,
        email = simulateLoanAggregate.customerInfo.customerEmail,
        birthDate = simulateLoanAggregate.customerInfo.birthDate,
        loanAmount = simulateLoanAggregate.simulation.loanAmount.value.toBigDecimal(),
        currency = simulateLoanAggregate.simulationResult.totalPayment.currency.code,
        months = simulateLoanAggregate.simulation.months.value,
        monthlyInstallment = simulateLoanAggregate.simulationResult.monthlyInstallment.toBigDecimal(),
        totalPayment = simulateLoanAggregate.simulationResult.totalPayment.toBigDecimal(),
        totalInterest = simulateLoanAggregate.simulationResult.totalInterest.toBigDecimal(),
        monthlyRate = simulateLoanAggregate.simulation.monthlyRate,
      )
  }
}
