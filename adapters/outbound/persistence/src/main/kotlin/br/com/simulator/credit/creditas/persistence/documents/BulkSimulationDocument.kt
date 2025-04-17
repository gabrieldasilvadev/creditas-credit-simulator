package br.com.simulator.credit.creditas.persistence.documents

import br.com.simulator.credit.creditas.commondomain.valueobjects.Money
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Document
data class BulkSimulationDocument(
  @Id
  val id: UUID,
  val status: BulkSimulationStatus,
  @field:Field("received_at")
  @field:CreatedDate
  val receivedAt: LocalDateTime? = null,
  val total: Int,
  val processed: Int = 0,
  val results: List<BulkSimulationResponseDto> = emptyList(),
)

data class BulkSimulationResponseDto(
  val source: Source,
  val target: Target,
) {
  data class Source(
    val amount: Money,
  )

  data class Target(
    val convertedAmount: Money,
    val totalPayment: Money,
    val monthlyInstallment: Money,
    val totalInterest: Money,
    val annualInterestRate: BigDecimal,
  )
}

enum class BulkSimulationStatus {
  PROCESSING,
  COMPLETED,
  FAILED,
}
