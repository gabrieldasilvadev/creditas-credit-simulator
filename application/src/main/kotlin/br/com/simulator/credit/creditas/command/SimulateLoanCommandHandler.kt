package br.com.simulator.credit.creditas.command

import br.com.simulator.credit.creditas.commondomain.DomainEventPublisher
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.factory.LoanAmountFactory
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.service.SimulateLoanService
import com.trendyol.kediatr.CommandWithResultHandler
import org.springframework.stereotype.Component

@Component
class SimulateLoanCommandHandler(
  private val domainEventPublisher: DomainEventPublisher,
  private val loanAmountFactory: LoanAmountFactory,
  private val simulationPersistencePort: SimulationPersistencePort,
) : CommandWithResultHandler<SimulateLoanCommand, LoanSimulationHttpResponse> {
  override suspend fun handle(command: SimulateLoanCommand): LoanSimulationHttpResponse {
    val loanAmount =
      loanAmountFactory.create(
        amount = command.amount,
        source = command.sourceCurrency,
        target = command.targetCurrency,
      )

    val applicant =
      CustomerInfo(
        birthDate = command.customerInfo.birthDate,
        customerEmail = command.customerInfo.customerEmail,
      )

    val simulation =
      SimulateLoanService.of(command.policyType).execute(
        LoanSimulationData.from(
          loanAmount = loanAmount.value,
          duration = command.termInMonths,
          applicant = applicant,
        ),
      )

    val aggregate =
      SimulateLoanAggregate.of(
        simulation = simulation,
        customerInfo = command.customerInfo,
      )

    runCatching {
      simulationPersistencePort.save(aggregate)
    }.onSuccess {
      domainEventPublisher.publishAll(simulation.getAndClearEvents())
    }.onFailure {
      println("Error saving simulation: ${it.message}")
    }.getOrThrow()

    val loanSimulationHttpResponse =
      LoanSimulationHttpResponse(
        source = LoanSimulationHttpResponse.Source(amount = command.amount),
        target =
          LoanSimulationHttpResponse.Target(
            convertedAmount = loanAmount.value,
            totalPayment = aggregate.simulationResult.totalPayment,
            monthlyInstallment = aggregate.simulationResult.monthlyInstallment,
            totalInterest = aggregate.simulationResult.totalInterest,
            annualInterestRate = command.policyType.annualInterestRate(applicant),
          ),
      )
    return loanSimulationHttpResponse
  }
}
