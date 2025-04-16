package br.com.simulator.credit.creditas.rest.controller

import br.com.simulator.credit.creditas.command.bulk.StartBulkSimulationCommand
import br.com.simulator.credit.creditas.persistence.adapter.BulkSimulationPersistenceAdapter
import br.com.simulator.credit.creditas.rest.config.PolicyConfiguration
import br.com.simulator.credit.creditas.simulationdomain.policy.PolicyType
import br.com.simulator.credit.openapi.web.api.CreditSimulationApi
import br.com.simulator.credit.openapi.web.dto.BulkLoanSimulationRequestDto
import br.com.simulator.credit.openapi.web.dto.BulkSimulationInitResponseDto
import br.com.simulator.credit.openapi.web.dto.BulkSimulationStatusResponseDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationRequestDto
import br.com.simulator.credit.openapi.web.dto.LoanSimulationResponseDto
import com.trendyol.kediatr.Mediator
import org.instancio.Instancio
import org.springframework.http.ResponseEntity
import java.util.UUID

class LoanSimulationController(
  private val mediator: Mediator,
  private val policyConfiguration: PolicyConfiguration,
  private val bulkSimulationRepository: BulkSimulationPersistenceAdapter,
) : CreditSimulationApi {
  override suspend fun simulateLoan(
    loanSimulationRequestDto: LoanSimulationRequestDto,
  ): ResponseEntity<LoanSimulationResponseDto> {
    val interestRatePolicy =
      policyConfiguration.resolve(
        PolicyType.entryOf(loanSimulationRequestDto.policyType.value),
      )
    val simulationHttpResponse = mediator.send(loanSimulationRequestDto.toCommand(interestRatePolicy))
    return ResponseEntity.ok(simulationHttpResponse.toResponseDto())
  }

  override suspend fun startBulkSimulation(
    bulkLoanSimulationRequestDto: BulkLoanSimulationRequestDto,
  ): ResponseEntity<BulkSimulationInitResponseDto> {
    val bulkId = UUID.randomUUID()
    val simulations =
      bulkLoanSimulationRequestDto.simulations.map {
        val interestRatePolicy = policyConfiguration.resolve(PolicyType.entryOf(it.policyType.value))
        it.toCommandDto(interestRatePolicy = interestRatePolicy)
      }.toList()
    mediator.send(StartBulkSimulationCommand(bulkId = bulkId, simulations = simulations))
    return ResponseEntity.accepted().body(
      BulkSimulationInitResponseDto(bulkId = bulkId, status = BulkSimulationInitResponseDto.Status.PROCESSING),
    )
  }

  override suspend fun getBulkSimulationStatus(bulkId: UUID): ResponseEntity<BulkSimulationStatusResponseDto> {
    val bulk =
      bulkSimulationRepository.findById(bulkId).orElseThrow()

    return ResponseEntity.ok(Instancio.create(BulkSimulationStatusResponseDto::class.java))
  }
}
