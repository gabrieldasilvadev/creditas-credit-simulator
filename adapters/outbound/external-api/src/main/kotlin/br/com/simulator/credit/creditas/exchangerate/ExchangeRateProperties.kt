package br.com.simulator.credit.creditas.exchangerate

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "external.exchange-rate")
class ExchangeRateProperties {
  lateinit var url: String
}
