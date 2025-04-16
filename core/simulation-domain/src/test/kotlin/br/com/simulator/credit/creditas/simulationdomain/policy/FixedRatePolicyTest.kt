package br.com.simulator.credit.creditas.simulationdomain.policy

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class FixedRatePolicyTest {
  private val fixedRate = BigDecimal("0.045")
  private val policy = FixedRatePolicy(fixedRate)

  @Test
  fun `should return fixed rate regardless of customer`() {
    val customer =
      CustomerInfo(
        birthDate = java.time.LocalDate.of(1990, 1, 1),
        customerEmail = "user@example.com",
      )

    val rate = policy.annualInterestRate(customer)

    assertEquals(fixedRate, rate)
  }
}
