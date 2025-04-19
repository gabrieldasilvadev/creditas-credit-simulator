package br.com.simulator.credit.creditas.container.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestTemplateConfiguration {
  @Bean
  fun restTemplate(): RestTemplate {
    return RestTemplateBuilder()
      .connectTimeout(Duration.ofSeconds(30))
      .build()
  }
}
