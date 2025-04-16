package br.com.simulator.credit.creditas.exchangerate.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["br.com.simulator.credit.creditas.exchangerate.client"])
class ClientConfiguration {
}
