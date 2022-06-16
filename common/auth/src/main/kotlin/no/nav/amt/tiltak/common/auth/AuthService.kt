package no.nav.amt.tiltak.common.auth

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.servlet.http.HttpServletRequest

@Service
open class AuthService(
	private val tokenValidationContextHolder: TokenValidationContextHolder
) {

	open fun isInternalRequest(httpServletRequest: HttpServletRequest): Boolean {
		return httpServletRequest.remoteAddr == "127.0.0.1"
	}

	open fun hentPersonligIdentTilInnloggetBruker(): String {
		val context = tokenValidationContextHolder.tokenValidationContext

		val token = context.firstValidToken.orElseThrow {
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized, valid token is missing")
		}

		return token.jwtTokenClaims["pid"]?.toString() ?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"PID is missing or is not a string"
		)
	}

	open fun hentNavIdentTilInnloggetBruker() : String = tokenValidationContextHolder
		.tokenValidationContext
		.getClaims(Issuer.AZURE_AD)
		.get("NAVident")
		?.toString() ?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"NAV ident is missing"
		)

	open fun hentAzureIdTilInnloggetBruker() : UUID = tokenValidationContextHolder
		.tokenValidationContext
		.getClaims(Issuer.AZURE_AD)
		.getStringClaim("oid").let { UUID.fromString(it) }
		?: throw ResponseStatusException(
		HttpStatus.UNAUTHORIZED,
		"oid is missing"
	)

}
