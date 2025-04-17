package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.commondomain.abstractions.Identifier
import java.util.UUID

data class LoanSimulationId(override val value: UUID = UUID.randomUUID()) : Identifier<UUID>
