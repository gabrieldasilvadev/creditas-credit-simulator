package br.com.simulator.credit.creditas.shared.messages

import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class BulkSimulationMessageTest {

    @Test
    fun `should create BulkSimulationMessage with empty simulations list`() {
        val bulkId = UUID.randomUUID()
        val requestedAt = LocalDateTime.now()

        val message = BulkSimulationMessage(
            bulkId = bulkId,
            simulations = emptyList(),
            requestedAt = requestedAt
        )

        assertEquals(bulkId, message.bulkId)
        assertEquals(0, message.simulations.size)
        assertEquals(requestedAt, message.requestedAt)
    }

    @Test
    fun `should create BulkSimulationMessage with default requestedAt`() {
        val bulkId = UUID.randomUUID()

        val message = BulkSimulationMessage(
            bulkId = bulkId,
            simulations = emptyList()
        )

        assertEquals(bulkId, message.bulkId)
        assertEquals(0, message.simulations.size)
        assertNotNull(message.requestedAt)
    }

    @Test
    fun `should create BulkSimulationMessage with simulations`() {
        val bulkId = UUID.randomUUID()
        val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "test@example.com")

        val simulation1 = BulkSimulationMessage.LoanSimulationMessage(
            loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
            customerInfo = customerInfo,
            months = 12,
            interestRate = Money(BigDecimal("0.03"), Currency.BRL),
            sourceCurrency = "BRL",
            targetCurrency = "USD",
            policyType = PolicyType.FIXED
        )

        val simulation2 = BulkSimulationMessage.LoanSimulationMessage(
            loanAmount = Money(BigDecimal("20000.00"), Currency.BRL),
            customerInfo = customerInfo,
            months = 24,
            interestRate = Money(BigDecimal("0.05"), Currency.BRL),
            sourceCurrency = "BRL",
            targetCurrency = "EUR",
            policyType = PolicyType.AGE_BASED
        )

        val message = BulkSimulationMessage(
            bulkId = bulkId,
            simulations = listOf(simulation1, simulation2)
        )

        assertEquals(bulkId, message.bulkId)
        assertEquals(2, message.simulations.size)

        assertEquals(BigDecimal("10000.00"), message.simulations[0].loanAmount.amount)
        assertEquals(Currency.BRL, message.simulations[0].loanAmount.currency)
        assertEquals(12, message.simulations[0].months)
        assertEquals(BigDecimal("0.03"), message.simulations[0].interestRate.amount)
        assertEquals("BRL", message.simulations[0].sourceCurrency)
        assertEquals("USD", message.simulations[0].targetCurrency)
        assertEquals(PolicyType.FIXED, message.simulations[0].policyType)

        assertEquals(BigDecimal("20000.00"), message.simulations[1].loanAmount.amount)
        assertEquals(24, message.simulations[1].months)
        assertEquals(BigDecimal("0.05"), message.simulations[1].interestRate.amount)
        assertEquals("EUR", message.simulations[1].targetCurrency)
        assertEquals(PolicyType.AGE_BASED, message.simulations[1].policyType)
    }

    @Test
    fun `should create LoanSimulationMessage with all properties`() {
        val customerInfo = CustomerInfo(LocalDate.of(1990, 1, 1), "test@example.com")

        val simulation = BulkSimulationMessage.LoanSimulationMessage(
            loanAmount = Money(BigDecimal("10000.00"), Currency.BRL),
            customerInfo = customerInfo,
            months = 12,
            interestRate = Money(BigDecimal("0.03"), Currency.BRL),
            sourceCurrency = "BRL",
            targetCurrency = "USD",
            policyType = PolicyType.FIXED
        )

        assertEquals(BigDecimal("10000.00"), simulation.loanAmount.amount)
        assertEquals(Currency.BRL, simulation.loanAmount.currency)
        assertEquals(customerInfo, simulation.customerInfo)
        assertEquals(12, simulation.months)
        assertEquals(BigDecimal("0.03"), simulation.interestRate.amount)
        assertEquals("BRL", simulation.sourceCurrency)
        assertEquals("USD", simulation.targetCurrency)
        assertEquals(PolicyType.FIXED, simulation.policyType)
    }
}
