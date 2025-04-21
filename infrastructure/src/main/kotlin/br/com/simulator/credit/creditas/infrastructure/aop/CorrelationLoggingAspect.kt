package br.com.simulator.credit.creditas.infrastructure.aop

import br.com.simulator.credit.creditas.infrastructure.CorrelationInterceptor.Companion.CORRELATION_ID
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Aspect
@Component
class CorrelationLoggingAspect {
  @Before("execution(* br.com.simulator.credit.creditas..*(..))")
  fun logCorrelationId() {
    val correlationId = MDC.get(CORRELATION_ID) ?: "undefined"
    MDC.put(CORRELATION_ID, correlationId)
  }
}
