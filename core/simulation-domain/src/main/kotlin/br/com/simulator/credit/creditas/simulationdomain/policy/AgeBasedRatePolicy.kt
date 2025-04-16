package br.com.simulator.credit.creditas.simulationdomain.policy

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class AgeBasedRatePolicy : InterestRatePolicy {
  override fun annualInterestRate(applicant: CustomerInfo): BigDecimal = applicant.ageBracket().annualInterestRate
}
