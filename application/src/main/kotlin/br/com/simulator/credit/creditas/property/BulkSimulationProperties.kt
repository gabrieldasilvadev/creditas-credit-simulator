package br.com.simulator.credit.creditas.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "internal.simulation.bulk")
data class BulkSimulationProperties(
  var size: Int = 10,
  var buffer: Int = 10,
)
