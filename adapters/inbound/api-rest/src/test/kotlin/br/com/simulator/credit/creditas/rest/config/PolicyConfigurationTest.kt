package br.com.simulator.credit.creditas.rest.config

import br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.AgeBasedRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.FixedRatePolicy
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

internal class PolicyConfigurationTest {
  private val fixedPolicy = FixedRatePolicy(BigDecimal("0.03"))
  private val ageBasedPolicy = AgeBasedRatePolicy()
  private val config = PolicyConfiguration(listOf(fixedPolicy, ageBasedPolicy))

  private val dummyApplication =
    CustomerInfo(
      birthDate = LocalDate.of(1990, 1, 1),
      customerEmail = "test@test.com",
    )

  @Test
  fun `should return fixed rate when policy type is FIXED`() {
    val policy = config.resolve(PolicyType.FIXED)
    val rate = policy.annualInterestRate(dummyApplication)
    assertEquals(BigDecimal("0.03"), rate)
  }

  @Test
  fun `should return age-based rate when policy type is AGE_BASED`() {
    val policy = config.resolve(PolicyType.AGE_BASED)
    val rate = policy.annualInterestRate(dummyApplication)
    assertEquals(BigDecimal("0.03"), rate)
  }
}
