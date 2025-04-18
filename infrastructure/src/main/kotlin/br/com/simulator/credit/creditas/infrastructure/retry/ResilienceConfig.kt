package br.com.simulator.credit.creditas.infrastructure.retry

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class ResilienceConfig {
  @Bean
  fun creditSimulationCircuitBreaker(): CircuitBreaker {
    val config =
      CircuitBreakerConfig.custom()
        .failureRateThreshold(50f)
        .waitDurationInOpenState(Duration.ofMillis(1000))
        .permittedNumberOfCallsInHalfOpenState(10)
        .slidingWindowSize(100)
        .build()

    return CircuitBreaker.of("creditSimulation", config)
  }

  @Bean
  fun creditSimulationRateLimiter(): RateLimiter {
    val config =
      RateLimiterConfig.custom()
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .limitForPeriod(10000)
        .timeoutDuration(Duration.ofMillis(25))
        .build()

    return RateLimiter.of("creditSimulation", config)
  }
}
