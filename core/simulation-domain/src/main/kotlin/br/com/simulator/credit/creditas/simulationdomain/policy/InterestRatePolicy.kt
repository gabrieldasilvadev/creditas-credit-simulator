package br.com.simulator.credit.creditas.simulationdomain.policy

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import java.math.BigDecimal

interface InterestRatePolicy {
  fun supports(type: PolicyType): Boolean
  fun annualInterestRate(applicant: CustomerInfo): BigDecimal
}
