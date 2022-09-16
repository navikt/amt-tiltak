package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.*
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val endringsmeldingService: EndringsmeldingService,
	private val navAnsattService: NavAnsattService,
	private val deltakerService: DeltakerService,
	private val tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService,
	private val authService: AuthService,
) {

	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<EndringsmeldingDto> {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		tiltaksansvarligAutoriseringService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAutoriseringService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId).map {
			val deltaker = deltakerService.hentDeltaker(it.deltakerId)
				?: throw NoSuchElementException("Fant ikke deltaker med id ${it.deltakerId}")

			return@map EndringsmeldingDto(
				id = it.id,
				startDato = it.startDato,
				aktiv = it.aktiv,
				godkjent = it.ferdiggjortAvNavAnsattId != null,
				arkivert = it.ferdiggjortAvNavAnsattId == null && !it.aktiv,
				opprettetAvArrangorAnsatt = arrangorAnsattService.getAnsatt(it.opprettetAvArrangorAnsattId).toDto(),
				bruker = deltaker.bruker.toDto(),
				opprettetDato = it.opprettet
			)
		}
	}


	@PatchMapping("/{endringsmeldingId}/ferdig")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun markerFerdig(@PathVariable("endringsmeldingId") endringsmeldingId: UUID) {
		val navAnsattAzureId = authService.hentAzureIdTilInnloggetBruker()
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		val endringsmelding = endringsmeldingService.hentEndringsmelding(endringsmeldingId)

		val deltaker = deltakerService.hentDeltaker(endringsmelding.deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

		tiltaksansvarligAutoriseringService.verifiserTilgangTilEndringsmelding(navAnsattAzureId)
		tiltaksansvarligAutoriseringService.verifiserTilgangTilGjennomforing(navIdent, deltaker.gjennomforingId)

		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		endringsmeldingService.markerSomFerdig(endringsmeldingId, navAnsatt.id)
	}

}
