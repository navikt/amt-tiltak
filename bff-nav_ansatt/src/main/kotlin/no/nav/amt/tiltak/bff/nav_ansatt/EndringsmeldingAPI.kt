package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.NavAnsattApiService.Companion.harTilgangTilDeltaker
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.response.MeldingerFraArrangorResponse
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.exceptions.UnauthorizedException
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.log.SecureLog.secureLog
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("EndringsmeldingAPINavAnsatt")
@RequestMapping("/api/nav-ansatt")
class EndringsmeldingAPI(
	private val endringsmeldingService: EndringsmeldingService,
	private val navAnsattService: NavAnsattService,
	private val deltakerService: DeltakerService,
	private val navAnsattApiService: NavAnsattApiService,
	private val tiltaksansvarligAuthService: TiltaksansvarligAutoriseringService,
	private val authService: AuthService,
) {

	@GetMapping("/endringsmelding")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<EndringsmeldingDto> {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilganger = authService.hentAdGrupperTilInnloggetBruker()

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return navAnsattApiService.hentEndringsmeldinger(gjennomforingId, tilganger)
	}

	@GetMapping("/meldinger")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentMeldingerFraArrangor(@RequestParam("gjennomforingId") gjennomforingId: UUID): MeldingerFraArrangorResponse {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilganger = authService.hentAdGrupperTilInnloggetBruker()

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return navAnsattApiService.hentMeldinger(gjennomforingId, tilganger)
	}

	@PatchMapping("/endringsmelding/{endringsmeldingId}/ferdig")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun markerFerdig(@PathVariable("endringsmeldingId") endringsmeldingId: UUID) {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilganger = authService.hentAdGrupperTilInnloggetBruker()
		val endringsmelding = endringsmeldingService.hentEndringsmelding(endringsmeldingId)

		val deltaker = deltakerService.hentDeltaker(endringsmelding.deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, deltaker.gjennomforingId)
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		if (harTilgangTilDeltaker(deltaker, tilganger)) {
			endringsmeldingService.markerSomUtfort(endringsmeldingId, navAnsatt.id)
		} else {
			secureLog.error(
				"nav ansatt: <${navAnsatt}> kan ikke arkivere endringsmelding på person " +
					"hvor skjermet = ${deltaker.erSkjermet} eller adressebeskyttelse = ${deltaker.harAdressebeskyttelse()}"
			)
			throw UnauthorizedException("Har ikke tilgang til operasjon på skjermet eller adressebeskyttet person. Sjekk secure logs")
		}
	}
}
