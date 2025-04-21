package br.com.simulator.credit.creditas.persistence.config

import io.mongock.driver.api.driver.ConnectionDriver
import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver
import io.mongock.runner.springboot.EnableMongock
import io.mongock.runner.springboot.MongockSpringboot
import io.mongock.runner.springboot.base.MongockInitializingBeanRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
@EnableMongock
class MongockConfiguration {
  @Bean
  fun connectionDriver(mongoTemplate: MongoTemplate): ConnectionDriver {
    return SpringDataMongoV4Driver.withDefaultLock(mongoTemplate)
  }

  @Bean
  fun mongockInitializingBeanRunner(
    connectionDriver: ConnectionDriver,
    applicationContext: ApplicationContext,
  ): MongockInitializingBeanRunner {
    return MongockSpringboot.builder()
      .setDriver(connectionDriver)
      .addMigrationScanPackage("br.com.simulator.credit.creditas.persistence.migrations")
      .setSpringContext(applicationContext)
      .buildInitializingBeanRunner()
  }
}
