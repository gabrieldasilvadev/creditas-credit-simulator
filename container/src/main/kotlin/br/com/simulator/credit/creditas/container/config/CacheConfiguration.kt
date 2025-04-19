package br.com.simulator.credit.creditas.container.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfiguration {
  @Bean
  fun caffeineCacheManager(): CacheManager {
    val cacheManager = CaffeineCacheManager("exchangeRateCache")
    cacheManager.setCaffeine(
      Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(100),
    )
    return cacheManager
  }
}
