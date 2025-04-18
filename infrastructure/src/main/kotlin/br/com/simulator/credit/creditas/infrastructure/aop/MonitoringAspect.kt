package br.com.simulator.credit.creditas.infrastructure.aop

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class MonitoringAspect(private val meterRegistry: MeterRegistry) {
  private val logger = LoggerFactory.getLogger(MonitoringAspect::class.java)

  @Around("@within(monitorable)")
  fun monitor(
    joinPoint: ProceedingJoinPoint,
    monitorable: Monitorable,
  ): Any? {
    val clazz = joinPoint.signature.declaringType.simpleName
    val method = joinPoint.signature.name
    val monitorName = monitorable.value.ifBlank { clazz }

    val tags =
      mutableListOf(
        Tag.of("class", monitorName),
        Tag.of("method", method),
      )

    val counter = meterRegistry.counter("method.calls", tags)
    val timer = meterRegistry.timer("method.execution", tags)

    counter.increment()

    return try {
      timer.recordCallable {
        joinPoint.proceed()
      }
    } catch (e: Exception) {
      meterRegistry.counter(
        "method.errors",
        Tags.of(tags).and("exception", e.javaClass.simpleName),
      ).increment()

      logger.error("Error during execution of $monitorName.$method", e)

      joinPoint.proceed()
    }
  }
}
