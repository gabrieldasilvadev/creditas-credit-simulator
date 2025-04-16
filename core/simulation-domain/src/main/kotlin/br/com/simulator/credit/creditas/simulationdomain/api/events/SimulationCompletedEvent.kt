package br.com.simulator.credit.creditas.simulationdomain.api.events

import br.com.simulator.credit.creditas.commondomain.DomainEvent
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.SimulationResult
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimulationCompletedEvent
  @JsonCreator
  constructor(
    @JsonProperty("loanSimulationData")
    val loanSimulationData: LoanSimulationInputDataEvent,
    @JsonProperty("result")
    val result: SimulationResultEvent,
    override val occurredOn: Long,
    override val aggregateId: String,
  ) : DomainEvent

data class SimulationResultEvent
  @JsonCreator
  constructor(
    @JsonProperty("totalPayment")
    val totalPayment: Money,
    @JsonProperty("monthlyInstallment")
    val monthlyInstallment: Money,
    @JsonProperty("totalInterest")
    val totalInterest: Money,
  ) {
    companion object {
      fun from(simulationResult: SimulationResult) =
        SimulationResultEvent(
          totalInterest = simulationResult.totalInterest,
          totalPayment = simulationResult.totalPayment,
          monthlyInstallment = simulationResult.monthlyInstallment,
        )
    }
  }

data class LoanSimulationInputDataEvent
  @JsonCreator
  constructor(
    @JsonProperty("email")
    val email: String,
    @JsonProperty("birthDate")
    val birthDate: String,
    @JsonProperty("loanAmount")
    val loanAmount: Money,
    @JsonProperty("currency")
    val currency: String,
    @JsonProperty("months")
    val months: Int,
  ) {
    companion object {
      fun from(loanSimulationData: LoanSimulationData) =
        LoanSimulationInputDataEvent(
          email = loanSimulationData.applicant.customerEmail,
          birthDate = loanSimulationData.applicant.birthDate.toString(),
          loanAmount = loanSimulationData.loanAmount,
          currency = loanSimulationData.loanAmount.currency.code,
          months = loanSimulationData.duration.value,
        )
    }
  }
