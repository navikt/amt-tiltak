package no.nav.amt.tiltak.application

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.amt.tiltak.core.exceptions.EndringsmeldingIkkeAktivException
import no.nav.amt.tiltak.core.exceptions.NotAuthenticatedException
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
open class ControllerAdvice(
	@Value("\${rest.include-stacktrace: false}") private val includeStacktrace: Boolean
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoSuchElementException::class)
	fun handleNotFoundException(e: NoSuchElementException): ResponseEntity<Response> {
		logger.info(e.message, e)

		return buildResponse(
			status = HttpStatus.NOT_FOUND,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(IllegalStateException::class)
	fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<Response> {
		logger.error(e.message, e)

		return buildResponse(
			status = HttpStatus.INTERNAL_SERVER_ERROR,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(NotAuthenticatedException::class)
	fun handleNotAuthenticatedException(e: NotAuthenticatedException): ResponseEntity<Response> {
		logger.info(e.message, e)

		return buildResponse(
			status = HttpStatus.UNAUTHORIZED,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(UnauthorizedException::class)
	fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<Response> {
		logger.info(e.message, e)

		return buildResponse(
			status = HttpStatus.FORBIDDEN,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ExceptionHandler(JwtTokenUnauthorizedException::class)
	fun handleJwtTokenUnauthorizedException(e: JwtTokenUnauthorizedException): ResponseEntity<Response> {
		logger.info(e.message, e)

		return buildResponse(
			status = HttpStatus.UNAUTHORIZED,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(ValidationException::class)
	fun handleValidationException(e: ValidationException): ResponseEntity<Response> {
		logger.info(e.message, e)

		return buildResponse(
			status = HttpStatus.BAD_REQUEST,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	@ExceptionHandler(NotImplementedError::class)
	fun handleNotImplementedError(e: NotImplementedError): ResponseEntity<Response> {
		return buildResponse(
			status = HttpStatus.NOT_IMPLEMENTED,
			exception = e
		)
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(EndringsmeldingIkkeAktivException::class)
	fun handleEndringsmeldingIkkeAktivException(e: EndringsmeldingIkkeAktivException): ResponseEntity<Response> {
		logger.error(e.message, e)

		return buildResponse(
			status = HttpStatus.BAD_REQUEST,
			exception = e
		)
	}

	private fun buildResponse(
		status: HttpStatus,
		exception: Throwable,
	): ResponseEntity<Response> {
		return ResponseEntity
			.status(status)
			.body(
				Response(
					status = status.value(),
					title = status,
					detail = exception.message,
					stacktrace = if (includeStacktrace) ExceptionUtils.getStackTrace(exception) else null
				)
			)

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	data class Response(
		val status: Int,
		val title: HttpStatus,
		val detail: String?,
		val stacktrace: String? = null
	)

}
