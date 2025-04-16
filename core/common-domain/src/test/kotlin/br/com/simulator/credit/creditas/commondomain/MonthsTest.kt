package br.com.simulator.credit.creditas.commondomain

import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

internal class MonthsTest {
  @ParameterizedTest
  @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12])
  fun `should convert to BigDecimal correctly`(value: Int) {
    val months = Months(value)
    assertEquals(BigDecimal(value), months.asBigDecimal)
  }

  @ParameterizedTest
  @ValueSource(ints = [0, -6, 13])
  fun `should reject zero months`(value: Int) {
    val exception =
      assertThrows(IllegalArgumentException::class.java) {
        Months(0)
      }
    assertEquals("Months must be between 1 and 12.", exception.message)
  }
}
