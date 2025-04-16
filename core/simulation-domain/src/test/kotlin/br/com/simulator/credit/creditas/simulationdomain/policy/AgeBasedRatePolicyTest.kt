package br.com.simulator.credit.creditas.simulationdomain.policy

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AgeBasedRatePolicyTest {
  private val policy = AgeBasedRatePolicy()

  @Test
  fun `should return correct rate based on customer age bracket`() {
    val customerInfo = mockk<CustomerInfo>()
    val expectedRate = BigDecimal("0.03")

    every { customerInfo.ageBracket().annualInterestRate } returns expectedRate

    val rate = policy.annualInterestRate(customerInfo)

    assertEquals(expectedRate, rate)
  }
}
