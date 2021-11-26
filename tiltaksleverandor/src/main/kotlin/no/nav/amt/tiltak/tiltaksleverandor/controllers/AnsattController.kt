package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattDTO
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.toDto
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/tiltaksleverandor/ansatt")
class AnsattController(
	private val tokenProvider: TokenValidationContextHolder,
	private val service: TiltaksleverandorService
) {

	@Protected
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): AnsattDTO {
		val context = tokenProvider.tokenValidationContext
		val token = context.firstValidToken.orElseThrow {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized, valid token is missing")
		}

		val personIdent = token.jwtTokenClaims["pid"]?.toString() ?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"PID is missing or is not a string."
		)

		val ansatt = service.getAnsattByPersonligIdent(personIdent)

		return ansatt.toDto()
	}

}
