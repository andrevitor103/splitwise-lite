package com.example.splitwise.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(
	val status: Int,
	val error: String,
	val message: String,
	val details: List<String> = emptyList(),
)

@RestControllerAdvice
class GlobalExceptionHandler {

	@ExceptionHandler(ApiException::class)
	fun handleApi(ex: ApiException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(ex.status).body(
			ErrorResponse(
				status = ex.status.value(),
				error = ex.status.reasonPhrase,
				message = ex.message,
			),
		)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
		val details = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
		return ResponseEntity.badRequest().body(
			ErrorResponse(
				status = HttpStatus.BAD_REQUEST.value(),
				error = HttpStatus.BAD_REQUEST.reasonPhrase,
				message = "Validation failed",
				details = details,
			),
		)
	}

	@ExceptionHandler(BadCredentialsException::class)
	fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
			ErrorResponse(
				status = HttpStatus.UNAUTHORIZED.value(),
				error = HttpStatus.UNAUTHORIZED.reasonPhrase,
				message = "Invalid email or password",
			),
		)
}
