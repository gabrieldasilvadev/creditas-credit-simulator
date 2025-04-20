package br.com.simulator.credit.creditas.persistence.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.util.UUID

@Configuration
@EnableMongoRepositories(basePackages = ["br.com.simulator.credit.creditas.persistence.repository"])
@EnableMongoAuditing
class MongoRepositoryConfiguration {
  @Bean
  fun auditorProvider(): AuditorAware<UUID> {
    return AuditorAwareImpl()
  }
}
