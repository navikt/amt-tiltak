package no.nav.amt.tiltak.endringsmelding
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.deltaker.dto.toDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val endringsmeldingService: EndringsmeldingService,
	private val veilederService: VeilederService,
	private val deltakerService: DeltakerService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val authService: AuthService
) {

	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<EndringsmeldingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		if (!tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		return endringsmeldingService.hentEndringsmeldinger(gjennomforingId).map { it.toDto() }
	}

	@PatchMapping("/{endringsmeldingId}/ferdig")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun markerFerdig(@PathVariable("endringsmeldingId") endringsmeldingId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		val endringsmelding = endringsmeldingService.hentEndringsmelding(endringsmeldingId)
		val deltaker = deltakerService.hentDeltaker(endringsmelding.deltakerId)

		if (!tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, deltaker.gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN)
		}

		val navAnsatt = veilederService.getNavAnsatt(navIdent)

		endringsmeldingService.markerSomFerdig(endringsmeldingId, navAnsatt.id)
	}

}
