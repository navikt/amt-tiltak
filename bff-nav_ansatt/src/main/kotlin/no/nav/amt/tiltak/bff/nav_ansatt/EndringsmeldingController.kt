package no.nav.amt.tiltak.bff.nav_ansatt

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

@RestController("EndringsmeldingControllerNavAnsatt")
@RequestMapping("/api/nav-ansatt")
class EndringsmeldingController(
	private val endringsmeldingService: EndringsmeldingService,
	private val navAnsattService: NavAnsattService,
	private val deltakerService: DeltakerService,
	private val controllerService: NavAnsattControllerService,
	private val tiltaksansvarligAuthService: TiltaksansvarligAutoriseringService,
	private val authService: AuthService,
) {

	@GetMapping("/endringsmelding")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<EndringsmeldingDto> {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilgangTilSkjermede = authService.harTilgangTilSkjermedePersoner()

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return controllerService.hentEndringsmeldinger(gjennomforingId, tilgangTilSkjermede)
	}

	@GetMapping("/meldinger")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentMeldingerFraArrangor(@RequestParam("gjennomforingId") gjennomforingId: UUID): MeldingerFraArrangorResponse {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilgangTilSkjermede = authService.harTilgangTilSkjermedePersoner()

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return controllerService.hentMeldinger(gjennomforingId, tilgangTilSkjermede)
	}

	@PatchMapping("/endringsmelding/{endringsmeldingId}/ferdig")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun markerFerdig(@PathVariable("endringsmeldingId") endringsmeldingId: UUID) {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilgangTilSkjermede = authService.harTilgangTilSkjermedePersoner()
		val endringsmelding = endringsmeldingService.hentEndringsmelding(endringsmeldingId)

		val deltaker = deltakerService.hentDeltaker(endringsmelding.deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

		tiltaksansvarligAuthService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAuthService.verifiserTilgangTilGjennomforing(navIdent, deltaker.gjennomforingId)
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		if (!tilgangTilSkjermede && deltaker.erSkjermet) {
			secureLog.error("nav ansatt: <${navAnsatt}> kan ikke arkivere endringsmelding på skjermet person")
			throw UnauthorizedException("Har ikke tilgang til operasjon på skjermet person. Sjekk secure logs")
		}

		if (deltaker.harAdressebeskyttelse()) {
			secureLog.error("nav ansatt: <${navAnsatt}> kan ikke arkivere endringsmelding på adressebeskyttet person")
			throw UnauthorizedException("Har ikke tilgang til operasjon på adressebeskyttet person. Sjekk secure logs")
		}

		endringsmeldingService.markerSomUtfort(endringsmeldingId, navAnsatt.id)
	}
}
