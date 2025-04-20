package br.com.simulator.credit.creditas.rest.exception

import br.com.simulator.credit.openapi.web.dto.ErrorResponseDto
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }

        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation failed",
            message = "There are validation errors in the request",
            path = ex.parameter.method?.name ?: "unknown",
            fieldErrors = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindException(ex: BindException): ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }

        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Binding failed",
            message = "There are binding errors in the request",
            path = "unknown",
            fieldErrors = errors
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid argument",
            message = ex.message ?: "Illegal argument",
            path = "unknown"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
            timestamp = LocalDateTime.now().atOffset(ZoneOffset.UTC),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal server error",
            message = ex.message ?: "Unexpected error",
            path = "unknown"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
