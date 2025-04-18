package br.com.simulator.credit.creditas.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "internal.simulation.bulk")
data class BulkSimulationConfig(
    var size: Int = 10,
    var buffer: Int = 10
)
