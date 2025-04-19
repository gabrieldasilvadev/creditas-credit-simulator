package br.com.simulator.credit.creditas.simulationdomain.policy

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class FixedRatePolicy(
  @Value("\${internal.interest.fixed.rate}") private val rate: BigDecimal,
) : InterestRatePolicy {
  override fun supports(type: PolicyType) = type == PolicyType.FIXED

  override fun annualInterestRate(applicant: CustomerInfo): BigDecimal = rate
}
