package br.com.simulator.credit.creditas.persistence.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackages = ["br.com.simulator.credit.creditas.persistence.repository"])
class MongoRepositoryConfiguration
