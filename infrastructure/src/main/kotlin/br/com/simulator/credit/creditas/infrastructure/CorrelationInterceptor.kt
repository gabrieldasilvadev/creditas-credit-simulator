package br.com.simulator.credit.creditas.infrastructure

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

@Component
class CorrelationInterceptor : HandlerInterceptor {
  companion object {
    const val CORRELATION_ID = "correlationId"
  }

  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
  ): Boolean {
    val correlationId = request.getHeader(CORRELATION_ID) ?: UUID.randomUUID().toString()
    MDC.put(CORRELATION_ID, correlationId)
    return true
  }

  override fun afterCompletion(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
    ex: Exception?,
  ) {
    MDC.clear()
  }
}
