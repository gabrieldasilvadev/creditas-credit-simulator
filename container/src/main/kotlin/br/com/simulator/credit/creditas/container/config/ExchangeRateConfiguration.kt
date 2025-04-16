package br.com.simulator.credit.creditas.container.config

import br.com.simulator.credit.creditas.exchangerate.ExchangeRateProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

@EnableConfigurationProperties(ExchangeRateProperties::class)
class ExchangeRateConfiguration
