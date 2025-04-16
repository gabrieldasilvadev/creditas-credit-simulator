package br.com.simulator.credit.creditas.simulationdomain.policy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PolicyTypeTest {
  @Test
  fun `should return enum when value is valid (case insensitive)`() {
    assertEquals(PolicyType.FIXED, PolicyType.entryOf("fixed"))
    assertEquals(PolicyType.FIXED, PolicyType.entryOf("FIXED"))
    assertEquals(PolicyType.AGE_BASED, PolicyType.entryOf("age"))
  }

  @Test
  fun `should throw exception when value is invalid`() {
    val ex =
      assertThrows(IllegalArgumentException::class.java) {
        PolicyType.entryOf("unknown")
      }

    assertTrue(ex.message!!.contains("Invalid policy type"))
  }
}
