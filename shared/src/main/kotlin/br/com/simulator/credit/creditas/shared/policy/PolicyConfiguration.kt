package br.com.simulator.credit.creditas.shared.policy

import br.com.simulator.credit.creditas.simulationdomain.policy.InterestRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import org.springframework.stereotype.Component

@Component
class PolicyConfiguration(
  private val policies: List<InterestRatePolicy>
) {
  fun resolve(type: PolicyType): InterestRatePolicy =
    policies.firstOrNull { it.supports(type) }
      ?: throw IllegalArgumentException("Unsupported policy type: $type")
}
