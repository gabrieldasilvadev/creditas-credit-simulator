package br.com.simulator.credit.creditas.rest.controller

import br.com.simulator.credit.creditas.command.SimulateLoanCommand
import br.com.simulator.credit.creditas.command.bulk.LoanSimulationCommandDto
import br.com.simulator.credit.creditas.commondomain.Currency
import br.com.simulator.credit.creditas.commondomain.Money
import br.com.simulator.credit.creditas.commondomain.toMoney
import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.openapi.web.dto.LoanSimulationRequestDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationResponseDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationResponseSourceDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationResponseTargetDto
import br.com.simulator.credit.openapi.web.dto.MoneyDto

fun LoanSimulationRequestDto.toCommand(interestRatePolicy: InterestRatePolicy) =
  SimulateLoanCommand(
    amount = this.loanAmount.amount.toMoney(),
    customerInfo = CustomerInfo(this.customerInfo.birthDate, this.customerInfo.email),
    termInMonths = Months(this.months),
    policyType = interestRatePolicy,
    sourceCurrency = Currency((this.sourceCurrency ?: Currency.BRL).toString()),
    targetCurrency = Currency((this.targetCurrency ?: Currency.BRL).toString()),
  )

fun LoanSimulationHttpResponse.toResponseDto(): LoanSimulationResponseDto =
  LoanSimulationResponseDto(
    source =
      LoanSimulationResponseSourceDto(
        MoneyDto(
          amount = this.source.amount.toString(),
          currency = this.source.amount.currency.toString(),
        ),
      ),
    target =
      LoanSimulationResponseTargetDto(
        convertedAmount =
          MoneyDto(
            amount = this.target.convertedAmount.toString(),
            currency = this.target.convertedAmount.currency.toString(),
          ),
        totalPayment =
          MoneyDto(
            amount = this.target.totalPayment.toString(),
            currency = this.target.totalPayment.currency.toString(),
          ),
        monthlyInstallment =
          MoneyDto(
            amount = this.target.monthlyInstallment.toString(),
            currency = this.target.monthlyInstallment.currency.toString(),
          ),
        totalInterest =
          MoneyDto(
            amount = this.target.totalInterest.toString(),
            currency = this.target.totalInterest.currency.toString(),
          ),
        annualInterestRate = this.target.annualInterestRate.toString(),
      ),
  )

fun LoanSimulationRequestDto.toCommandDto(interestRatePolicy: InterestRatePolicy): LoanSimulationCommandDto =
  LoanSimulationCommandDto(
    loanAmount = Money(this.loanAmount.amount, Currency(this.loanAmount.currency)),
    customerInfo =
      CustomerInfo(
        birthDate = this.customerInfo.birthDate,
        customerEmail = this.customerInfo.email,
      ),
    months = this.months,
    policyType = interestRatePolicy,
    sourceCurrency = this.sourceCurrency ?: Currency.BRL.code,
    targetCurrency = this.targetCurrency ?: Currency.BRL.code,
  )
