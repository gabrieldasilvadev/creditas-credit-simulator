package br.com.simulator.credit.creditas.container.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
  @Bean(name = ["eventTaskExecutor"])
  fun eventTaskExecutor(): Executor {
    val executor = ThreadPoolTaskExecutor()
    executor.corePoolSize = 4
    executor.maxPoolSize = 10
    executor.queueCapacity = 100
    executor.threadNamePrefix = "event-pool-"
    executor.initialize()
    return executor
  }
}
