package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("EndringsmeldingControllerNavAnsatt")
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingController(
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

		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val deltakerMap = deltakerService.hentDeltakerMap(endringsmeldinger.map { it.deltakerId })

		return endringsmeldinger.map {
			val deltaker = deltakerMap[it.deltakerId]
				?: throw NoSuchElementException("Fant ikke deltaker med id ${it.deltakerId}")

			return@map EndringsmeldingDto(
				id = it.id,
				deltaker = DeltakerDto(
					deltaker.fornavn,
					deltaker.mellomnavn,
					deltaker.etternavn,
					deltaker.fodselsnummer,
				),
				status = it.status.toDto(),
				innhold = it.innhold.toDto(),
				opprettetDato = it.opprettet,
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

		endringsmeldingService.markerSomUtfort(endringsmeldingId, navAnsatt.id)
	}

}
