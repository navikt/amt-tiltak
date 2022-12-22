package no.nav.amt.tiltak.common.auth

import no.nav.amt.tiltak.core.exceptions.NotAuthenticatedException
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
open class AuthService(
	private val tokenValidationContextHolder: TokenValidationContextHolder
) {
	@Value("\${ad_gruppe_tilgang_til_egne_ansatte}")
	lateinit var tilgangTilNavAnsattGroupId: String

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

	open fun hentNavIdentTilInnloggetBruker(): String = tokenValidationContextHolder
		.tokenValidationContext
		.getClaims(Issuer.AZURE_AD)
		.get("NAVident")
		?.toString()
		?: throw NotAuthenticatedException("NAV ident is missing")

	open fun harTilgangTilSkjermedePersoner(): Boolean = tokenValidationContextHolder
		.tokenValidationContext
		.getClaims(Issuer.AZURE_AD)
		.getAsList("groups").let { groups -> groups.any { it == tilgangTilNavAnsattGroupId } }

	open fun hentAzureIdTilInnloggetBruker(): UUID = tokenValidationContextHolder
		.tokenValidationContext
		.getClaims(Issuer.AZURE_AD)
		.getStringClaim("oid").let { UUID.fromString(it) }
		?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"oid is missing"
		)

}
