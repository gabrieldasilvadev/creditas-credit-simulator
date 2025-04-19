package br.com.simulator.credit.creditas.container.config.web

import br.com.simulator.credit.creditas.infrastructure.CorrelationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
  private val correlationInterceptor: CorrelationInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(correlationInterceptor)
  }
}
