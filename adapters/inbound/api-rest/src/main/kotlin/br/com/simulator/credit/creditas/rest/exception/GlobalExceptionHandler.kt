package br.com.simulator.credit.creditas.rest.exception

import br.com.simulator.credit.openapi.web.dto.ErrorResponseDto
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestControllerAdvice
class GlobalExceptionHandler {
  private val logger = LoggerFactory.getLogger(this::class.java)

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
    val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }

    val errorResponse =
      ErrorResponseDto(
        timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
        status = HttpStatus.BAD_REQUEST.name,
        type = "Validation failed",
        message = "There are validation errors in the request",
        path = ex.parameter.method?.name ?: "unknown",
        details = errors,
      )

    logger.error("Validation error: ${errorResponse.details}", ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(BindException::class)
  fun handleBindException(
    ex: BindException,
    httpServletRequest: HttpServletRequest,
  ): ResponseEntity<ErrorResponseDto> {
    val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }

    val errorResponse =
      ErrorResponseDto(
        timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
        status = HttpStatus.BAD_REQUEST.name,
        type = "Binding failed",
        message = "There are binding errors in the request",
        path = httpServletRequest.requestURI,
        details = errors,
      )

    logger.error("Binding error: ${errorResponse.details}", ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgument(
    ex: IllegalArgumentException,
    httpServletRequest: HttpServletRequest,
  ): ResponseEntity<ErrorResponseDto> {
    val errors = mapOf("error" to (ex.message ?: "Illegal argument"))
    val errorResponse =
      ErrorResponseDto(
        timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
        status = HttpStatus.BAD_REQUEST.name,
        type = "Invalid argument",
        message = ex.message ?: "Illegal argument",
        path = httpServletRequest.requestURI,
        details = errors,
      )

    logger.error("Illegal argument error: ${errorResponse.message}", ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(Exception::class)
  fun handleGenericException(
    ex: Exception,
    httpServletRequest: HttpServletRequest,
  ): ResponseEntity<ErrorResponseDto> {
    val errorResponse =
      ErrorResponseDto(
        timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
        status = HttpStatus.INTERNAL_SERVER_ERROR.name,
        type = "Internal server error",
        message = ex.stackTrace.contentToString() ?: "Internal server error",
        path = httpServletRequest.requestURI,
        details = null,
      )

    logger.error("Internal server error: ${errorResponse.message}")
    return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }
}
