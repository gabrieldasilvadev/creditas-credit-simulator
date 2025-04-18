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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class StartBulkSimulationCommandHandlerTest {

  private val repository: BulkSimulationPersistenceAdapter = mockk(relaxed = true)
  private val mediator: Mediator = mockk(relaxed = true)
  private val config = BulkSimulationConfig(size = 3, buffer = 2)

  private lateinit var handler: StartBulkSimulationCommandHandler

  @BeforeTest
  fun setup() {
    handler = StartBulkSimulationCommandHandler(repository, mediator, config)
  }

  @Test
  fun `should initialize document if not found`() = runTest {
    val simulations = listOf(
      LoanSimulationCommandDto(
        loanAmount = Money(BigDecimal(1000), Currency("BRL")),
        customerInfo = CustomerInfo(birthDate = LocalDate.of(1990, 1, 1), customerEmail = "test@x.com"),
        policyType = FixedRatePolicy(BigDecimal("0.3")),
        months = 12,
        sourceCurrency = "BRL",
        targetCurrency = "BRL"
      )
    )

    val command = StartBulkSimulationCommand(UUID.randomUUID(), simulations)
    val savedDocuments = mutableListOf<BulkSimulationDocument>()

    every { repository.findById(any()) } answers {
      if (savedDocuments.isEmpty()) Optional.empty()
      else Optional.of(savedDocuments.last())
    }

    every { repository.save(any()) } answers {
      val doc = firstArg<BulkSimulationDocument>()
      savedDocuments.add(doc)
      doc
    }

    coEvery { mediator.send<SimulateLoanCommand, LoanSimulationHttpResponse>(any()) } returns LoanSimulationHttpResponse(
      source = LoanSimulationHttpResponse.Source(Money(BigDecimal(1000), Currency("BRL"))),
      target = LoanSimulationHttpResponse.Target(
        convertedAmount = Money(BigDecimal(1000), Currency("BRL")),
        totalPayment = Money("1000"),
        monthlyInstallment = Money("1000"),
        totalInterest = Money("1000"),
        annualInterestRate = BigDecimal(0.3)
      )
    )

    handler.handle(command)

    verify { repository.save(match { it.id == command.bulkId && it.status == BulkSimulationStatus.PROCESSING }) }
    verify { repository.save(match { it.status == BulkSimulationStatus.COMPLETED }) }
  }

  @Test
  fun `should update document status to FAILED on exception`() = runTest {
    val simulations = listOf(
      LoanSimulationCommandDto(
        loanAmount = Money(BigDecimal(1000), Currency("BRL")),
        customerInfo = CustomerInfo(birthDate = LocalDate.of(1990, 1, 1), customerEmail = "test@x.com"),
        policyType = FixedRatePolicy(BigDecimal("0.3")),
        months = 12,
        sourceCurrency = "BRL",
        targetCurrency = "BRL"
      )
    )
    val command = StartBulkSimulationCommand(UUID.randomUUID(), simulations)

    every { repository.findById(any()) } returns Optional.of(
      BulkSimulationDocument(
        id = command.bulkId,
        status = BulkSimulationStatus.PROCESSING,
        processed = 0,
        total = simulations.size
      )
    )
    coEvery { mediator.send(any(SimulateLoanCommand::class)) } throws RuntimeException("Simulation error")

    handler.handle(command)

    verify { repository.save(match { it.id == command.bulkId && it.status == BulkSimulationStatus.FAILED }) }
  }

  @Test
  fun `should process simulations in batches respecting buffer size`() = runTest {
    val simulations = (1..10).map {
      LoanSimulationCommandDto(
        loanAmount = Money(BigDecimal(1000), Currency("BRL")),
        customerInfo = CustomerInfo(birthDate = LocalDate.of(1990, 1, 1), customerEmail = "test@x.com"),
        policyType = FixedRatePolicy(BigDecimal("0.3")),
        months = 12,
        sourceCurrency = "BRL",
        targetCurrency = "BRL"
      )
    }
    val command = StartBulkSimulationCommand(UUID.randomUUID(), simulations)

    val savedDocuments = mutableListOf<BulkSimulationDocument>()

    every { repository.findById(any()) } answers {
      if (savedDocuments.isEmpty()) Optional.empty()
      else Optional.of(savedDocuments.last { it.id == firstArg<UUID>() })
    }

    every { repository.save(any()) } answers {
      val doc = firstArg<BulkSimulationDocument>()
      savedDocuments.removeIf { it.id == doc.id }
      savedDocuments.add(doc)
      doc
    }

    coEvery { mediator.send<SimulateLoanCommand, LoanSimulationHttpResponse>(any()) } returns LoanSimulationHttpResponse(
      source = LoanSimulationHttpResponse.Source(Money(BigDecimal(1000), Currency("BRL"))),
      target = LoanSimulationHttpResponse.Target(
        convertedAmount = Money(BigDecimal(1000), Currency("BRL")),
        totalPayment = Money("1000"),
        monthlyInstallment = Money("1000"),
        totalInterest = Money("1000"),
        annualInterestRate = BigDecimal(0.3)
      )
    )

    handler.handle(command)

    verify(atLeast = 4) { repository.save(match { it.results.isNotEmpty() }) }
    verify { repository.save(match { it.status == BulkSimulationStatus.COMPLETED }) }
  }
}
