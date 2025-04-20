package br.com.simulator.credit.creditas.rest.controller

import br.com.simulator.credit.creditas.command.bulk.StartBulkSimulationCommand
import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.shared.policy.PolicyConfiguration
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import br.com.simulator.credit.openapi.web.api.CreditSimulationApi
import br.com.simulator.credit.openapi.web.dto.BulkLoanSimulationRequestDto
import br.com.simulator.credit.openapi.web.dto.BulkSimulationInitResponseDto
import br.com.simulator.credit.openapi.web.dto.BulkSimulationStatusResponseDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationRequestDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationResponseDto
import com.trendyol.kediatr.Mediator
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
@Monitorable("LoanSimulationController")
class LoanSimulationController(
  private val mediator: Mediator,
  private val policyConfiguration: PolicyConfiguration,
  private val bulkSimulationRepository: BulkSimulationPersistenceAdapter,
) : CreditSimulationApi {

  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun simulateLoan(
    loanSimulationRequestDto: LoanSimulationRequestDto,
  ): ResponseEntity<LoanSimulationResponseDto> {
    logger.info("Received simulation request: $loanSimulationRequestDto")
    val policyType = loanSimulationRequestDto.policyType?.value ?: PolicyType.FIXED.value
    val interestRatePolicy = policyConfiguration.resolve(PolicyType.entryOf(policyType))
    val simulationHttpResponse = mediator.send(loanSimulationRequestDto.toCommand(interestRatePolicy))
    return ResponseEntity.ok(simulationHttpResponse.toResponseDto()).also {
      logger.info("Simulation response: ${it.body}")
    }
  }

  override suspend fun startBulkSimulation(
    bulkLoanSimulationRequestDto: BulkLoanSimulationRequestDto,
  ): ResponseEntity<BulkSimulationInitResponseDto> {
    logger.info("Received bulk simulation request: $bulkLoanSimulationRequestDto")
    val bulkId = UUID.randomUUID()
    val simulations =
      bulkLoanSimulationRequestDto.simulations.map {
        val policyType = PolicyType.entryOf(it.policyType?.value ?: PolicyType.FIXED.value)
        val interestRatePolicy = policyConfiguration.resolve(policyType)
        it.toCommandDto(interestRatePolicy)
      }

    mediator.send(StartBulkSimulationCommand(bulkId = bulkId, simulations = simulations))

    return ResponseEntity.accepted().body(
      BulkSimulationInitResponseDto(
        bulkId = bulkId,
        status = BulkSimulationInitResponseDto.Status.PROCESSING,
      ),
    ).also {
      logger.info("Bulk simulation response: ${it.body}")
    }
  }

  override suspend fun getBulkSimulationStatus(bulkId: UUID): ResponseEntity<BulkSimulationStatusResponseDto> {
    logger.info("Getting bulk simulation status for ID: $bulkId")
    val bulk =
      bulkSimulationRepository.findById(bulkId).orElseThrow()

    return ResponseEntity.ok(bulk.toResponse()).also {
      logger.info("Bulk simulation status response: ${it.body}")
    }
  }
}
