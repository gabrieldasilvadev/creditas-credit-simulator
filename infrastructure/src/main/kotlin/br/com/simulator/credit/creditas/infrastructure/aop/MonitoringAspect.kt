package br.com.simulator.credit.creditas.infrastructure.aop

import br.com.simulator.credit.creditas.infrastructure.annotations.Monitorable
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
class MonitoringAspect(private val meterRegistry: MeterRegistry) {
  private val logger = LoggerFactory.getLogger(MonitoringAspect::class.java)

  private inline fun safe(action: () -> Unit) {
    try {
      action()
    } catch (t: Throwable) {
      logger.debug("Falha ao registrar métrica: ${t.message}")
    }
  }

  @Around("@within(monitorable)")
  fun monitor(
    joinPoint: ProceedingJoinPoint,
    monitorable: Monitorable,
  ): Any? {
    val clazz = joinPoint.signature.declaringType.simpleName
    val method = joinPoint.signature.name
    val monitorName = monitorable.value.ifBlank { clazz }
    val tags = Tags.of("class", monitorName, "method", method)

    val start = System.nanoTime()
    var thrown: Throwable? = null

    try {
      return joinPoint.proceed()
    } catch (t: Throwable) {
      thrown = t
      throw t
    } finally {
      val elapsed = System.nanoTime() - start

      safe { meterRegistry.counter("method.calls", tags).increment() }
      safe {
        meterRegistry.timer("method.execution", tags)
          .record(elapsed, TimeUnit.NANOSECONDS)
      }

      if (thrown == null) {
        safe { meterRegistry.counter("method.success", tags).increment() }
      } else {
        val errorTags = tags.and("exception", thrown.javaClass.simpleName)
        safe { meterRegistry.counter("method.errors", errorTags).increment() }
      }
    }
  }
}
