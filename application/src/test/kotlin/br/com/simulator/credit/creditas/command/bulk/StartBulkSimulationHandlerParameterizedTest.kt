package br.com.simulator.credit.creditas.command.bulk

import br.com.simulator.credit.creditas.command.SimulateLoanCommand
import br.com.simulator.credit.creditas.commondomain.valueobjects.Currency
import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import br.com.simulator.credit.creditas.config.BulkSimulationConfig
import br.com.simulator.credit.creditas.dto.LoanSimulationHttpResponse
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationDocument
import br.com.simulator.credit.creditas.persistence.documents.BulkSimulationStatus
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import br.com.simulator.credit.creditas.simulationdomain.policy.FixedRatePolicy
import com.trendyol.kediatr.Mediator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import java.util.stream.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@ExperimentalCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class StartBulkSimulationHandlerParameterizedTest {

  private lateinit var repository: BulkSimulationPersistenceAdapter
  private lateinit var mediator: Mediator

  companion object {
    @JvmStatic
    fun configProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(2, 1, 5),
      Arguments.of(3, 2, 4),
      Arguments.of(5, 3, 2),
      Arguments.of(5, 5, 2),
    )
  }

  @BeforeEach
  fun setup() {
    repository = mockk(relaxed = true)
    mediator = mockk(relaxed = true)
  }

  @ParameterizedTest
  @MethodSource("configProvider")
  fun `should send all simulations and batch correctly`(
    bulkSize: Int,
    bufferSize: Int,
    expectedBatches: Int
  ) = runTest {
    val config = BulkSimulationConfig(size = bulkSize, buffer = bufferSize)
    val handler = StartBulkSimulationCommandHandler(repository, mediator, config)

    val simulations = (1..10).map {
      LoanSimulationCommandDto(
        loanAmount = Money("1000.00"),
        customerInfo = CustomerInfo(
          LocalDate.of(1990, 1, 1),
          "test@example.com"
        ),
        months = 12,
        policyType = FixedRatePolicy("0.3".toBigDecimal()),
        sourceCurrency = "BRL",
        targetCurrency = "BRL"
      )
    }
    val bulkId = UUID.randomUUID()
    val command = StartBulkSimulationCommand(bulkId, simulations)

    var savedDocument: BulkSimulationDocument? = null

    every { repository.findById(bulkId) } answers {
      savedDocument?.let { Optional.of(it) } ?: Optional.empty()
    }

    every { repository.save(any()) } answers {
      savedDocument = firstArg()
      firstArg()
    }

    coEvery { mediator.send(any(SimulateLoanCommand::class)) } returns mockLoanSimulationHttpResponse()

    handler.handle(command)

    coVerify(exactly = simulations.size) { mediator.send(any<SimulateLoanCommand>()) }
    verify(atLeast = expectedBatches) { repository.save(match { it.results.isNotEmpty() }) }
    verify { repository.save(match { it.status == BulkSimulationStatus.COMPLETED }) }
  }

  private fun mockLoanSimulationHttpResponse(): LoanSimulationHttpResponse = LoanSimulationHttpResponse(
    source = LoanSimulationHttpResponse.Source(
      Money(
        BigDecimal(1000),
        Currency("BRL")
      )
    ),
    target = LoanSimulationHttpResponse.Target(
      convertedAmount = Money(BigDecimal(1000), Currency("BRL")),
      totalPayment = Money("1000"),
      monthlyInstallment = Money("1000"),
      totalInterest = Money("1000"),
      annualInterestRate = BigDecimal(0.3)
    )
  )
}
