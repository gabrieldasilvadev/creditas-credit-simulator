package br.com.simulator.credit.creditas.command.single

import br.com.simulator.credit.creditas.command.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.command.factory.LoanAmountFactory
import br.com.simulator.credit.creditas.commondomain.abstractions.DomainEventPublisher
import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.simulationdomain.model.SimulateLoanAggregate
import br.com.simulator.credit.creditas.simulationdomain.model.ports.SimulationPersistencePort
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.LoanSimulationData
import br.com.simulator.credit.creditas.simulationdomain.service.SimulateLoanService
import com.trendyol.kediatr.CommandWithResultHandler
import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Monitorable
class SimulateLoanCommandHandler(
  private val domainEventPublisher: DomainEventPublisher,
  private val loanAmountFactory: LoanAmountFactory,
  private val simulationPersistencePort: SimulationPersistencePort,
) : CommandWithResultHandler<SimulateLoanCommand, LoanSimulationHttpResponse> {
  private val logger: Logger = LoggerFactory.getLogger(SimulateLoanCommandHandler::class.java)

  @CircuitBreaker(name = "simulate-circuit")
  @Bulkhead(name = "simulate-bulkhead", type = Bulkhead.Type.SEMAPHORE)
  override suspend fun handle(command: SimulateLoanCommand): LoanSimulationHttpResponse {
    logger.info("Starting loan simulation: $command")
    val loanAmount = loanAmountFactory.create(command.amount, command.sourceCurrency, command.targetCurrency)
    val applicant = CustomerInfo(command.customerInfo.birthDate, command.customerInfo.customerEmail)
    val annualInterestRate = command.interestRatePolicy.annualInterestRate(applicant)
    val simulation =
      SimulateLoanService.execute(
        LoanSimulationData.from(loanAmount.value, command.termInMonths, applicant, annualInterestRate.toMoney()),
      )

    logger.info("Loan simulation completed: $simulation")

    val aggregate = SimulateLoanAggregate.of(simulation, command.customerInfo)

    runCatching { simulationPersistencePort.save(aggregate) }
      .onSuccess { domainEventPublisher.publishAll(simulation.getAndClearEvents()) }
      .onFailure { logger.error("Error saving simulation: ${it.message} | cause: ${it.cause}") }

    logger.info("Loan simulation saved successfully: $aggregate")

    return LoanSimulationHttpResponse(
      source = LoanSimulationHttpResponse.Source(command.amount),
      target =
        LoanSimulationHttpResponse.Target(
          convertedAmount = loanAmount.value,
          totalPayment = aggregate.simulationResult.totalPayment,
          monthlyInstallment = aggregate.simulationResult.monthlyInstallment,
          totalInterest = aggregate.simulationResult.totalInterest,
          annualInterestRate = command.interestRatePolicy.annualInterestRate(applicant).toMoney(),
        ),
    )
  }
}
