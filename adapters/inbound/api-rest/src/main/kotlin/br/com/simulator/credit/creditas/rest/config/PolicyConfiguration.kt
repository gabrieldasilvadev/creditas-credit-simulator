package br.com.simulator.credit.creditas.rest.config

import br.com.simulator.credit.creditas.simulationdomain.policy.AgeBasedRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.FixedRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import org.springframework.stereotype.Component

@Component
class PolicyConfiguration(
  private val fixed: FixedRatePolicy,
  private val ageBased: AgeBasedRatePolicy,
) {
  fun resolve(type: PolicyType): InterestRatePolicy =
    when (type) {
      PolicyType.FIXED -> fixed
      PolicyType.AGE_BASED -> ageBased
    }
}
