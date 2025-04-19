package br.com.simulator.credit.creditas.commondomain

import br.com.simulator.credit.creditas.commondomain.valueobjects.Months
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class MonthsTest {

    @Test
    fun `should expose integer value`() {
        val months = Months(12)
        assertEquals(12, months.value)
    }

    @Test
    fun `should convert value to BigDecimal`() {
        val months = Months(12)
        val expected = BigDecimal("12")
        assertEquals(expected, months.asBigDecimal)
    }
}
