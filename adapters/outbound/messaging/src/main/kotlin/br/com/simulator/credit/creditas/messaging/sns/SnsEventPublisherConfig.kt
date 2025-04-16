package br.com.simulator.credit.creditas.messaging.sns

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.sns.SnsClient

@Configuration
class SnsEventPublisherConfig {
  @Bean
  fun snsEventPublisher(
    snsClient: SnsClient,
    objectMapper: ObjectMapper,
  ): SnsEventPublisher {
    return SnsEventPublisher(snsClient, objectMapper)
  }
}
