package no.nav.amt.tiltak.common.auth

import no.nav.amt.tiltak.core.exceptions.NotAuthenticatedException
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
open class AuthService(
	private val tokenValidationContextHolder: TokenValidationContextHolder
) {
	@Value("\${ad_gruppe_tilgang_til_egne_ansatte}")
	lateinit var tilgangTilNavAnsattGroupId: String

	@Value("\${ad_gruppe_tiltak_ansvarlig}")
	lateinit var tiltakAnsvarligGroupId: String

	@Value("\${ad_gruppe_endringsmelding}")
	lateinit var endringsmeldingGroupId: String

	@Value("\${ad_gruppe_fortrolig_adresse}")
	lateinit var fortroligAdresseGroupId: String

	@Value("\${ad_gruppe_strengt_fortrolig_adresse}")
	lateinit var strengtFortroligAdresseGroupId: String

	open fun hentPersonligIdentTilInnloggetBruker(): String {
		val context = tokenValidationContextHolder.getTokenValidationContext()

		val token = context.firstValidToken ?:
			throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authorized, valid token is missing")

		return token.jwtTokenClaims.getStringClaim("pid") ?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"PID is missing or is not a string"
		)
	}

	open fun hentNavIdentTilInnloggetBruker(): String = tokenValidationContextHolder
		.getTokenValidationContext()
		.getClaims(Issuer.AZURE_AD)
		.getStringClaim("NAVident")
		?: throw NotAuthenticatedException("NAV ident is missing")

	open fun harTilgangTilTiltaksansvarligflate() = harTilgangTilADGruppe(tiltakAnsvarligGroupId)

	open fun harTilgangTilEndringsmeldinger() = harTilgangTilADGruppe(endringsmeldingGroupId)

	fun validerErM2MToken() {
		if(!erM2MToken()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Action only permitted by m2m token")
	}

	open fun hentAzureIdTilInnloggetBruker(): UUID = tokenValidationContextHolder
		.getTokenValidationContext()
		.getClaims(Issuer.AZURE_AD)
		.getStringClaim("oid").let { UUID.fromString(it) }
		?: throw ResponseStatusException(
			HttpStatus.UNAUTHORIZED,
			"oid is missing"
		)

	fun hentAdGrupperTilInnloggetBruker(): List<AdGruppe> = tokenValidationContextHolder
		.getTokenValidationContext()
		.getClaims(Issuer.AZURE_AD)
		.getAsList("groups")
		.mapNotNull(this::mapIdTilAdGruppe)

	private fun erM2MToken() = hentAzureIdTilInnloggetBruker().equals(hentSubjectClaim())

	private fun hentSubjectClaim(): UUID = tokenValidationContextHolder
		.getTokenValidationContext()
		.getClaims(Issuer.AZURE_AD)
		.getStringClaim("sub").let { UUID.fromString(it.toString()) }
		?: throw NotAuthenticatedException("Sub is missing")

	private fun harTilgangTilADGruppe(id: String): Boolean = tokenValidationContextHolder
		.getTokenValidationContext()
		.getClaims(Issuer.AZURE_AD)
		.getAsList("groups")
		.let { groups -> groups.any { it == id }}

	private fun mapIdTilAdGruppe(id: String?): AdGruppe? {
		return when(id) {
			tiltakAnsvarligGroupId -> AdGruppe.TILTAKSANSVARLIG_FLATE_GRUPPE
			endringsmeldingGroupId -> AdGruppe.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
			tilgangTilNavAnsattGroupId -> AdGruppe.TILTAKSANSVARLIG_EGNE_ANSATTE_GRUPPE
			fortroligAdresseGroupId -> AdGruppe.TILTAKSANSVARLIG_FORTROLIG_ADRESSE_GRUPPE
			strengtFortroligAdresseGroupId -> AdGruppe.TILTAKSANSVARLIG_STRENGT_FORTROLIG_ADRESSE_GRUPPE
			else -> null
		}
	}

}
