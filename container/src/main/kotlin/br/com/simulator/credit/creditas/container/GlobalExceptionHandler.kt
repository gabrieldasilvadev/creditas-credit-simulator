package br.com.simulator.credit.creditas.container

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
  private val logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

  @ExceptionHandler(Exception::class)
  fun handle(e: Exception): ResponseEntity<String> {
    logger.error("Erro na simulação", e)
    return ResponseEntity.status(500).body("Erro interno")
  }
}
