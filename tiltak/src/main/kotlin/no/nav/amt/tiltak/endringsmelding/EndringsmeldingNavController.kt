package no.nav.amt.tiltak.endringsmelding
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligAutoriseringService
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.deltaker.dto.toDto
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/endringsmelding")
class EndringsmeldingNavController(
	private val endringsmeldingService: EndringsmeldingService,
	private val navAnsattService: NavAnsattService,
	private val deltakerService: DeltakerService,
	private val tiltaksansvarligAutoriseringService: TiltaksansvarligAutoriseringService,
	private val authService: AuthService,
) {

	@GetMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentEndringsmeldinger(@RequestParam("gjennomforingId") gjennomforingId: UUID) : List<EndringsmeldingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		tiltaksansvarligAutoriseringService.verifiserTilgangTilEndringsmelding(navIdent)
		tiltaksansvarligAutoriseringService.verifiserTilgangTilGjennomforing(navIdent, gjennomforingId)

		return endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId).map { it.toDto() }
	}

	@PatchMapping("/{endringsmeldingId}/ferdig")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun markerFerdig(@PathVariable("endringsmeldingId") endringsmeldingId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val endringsmelding = endringsmeldingService.hentEndringsmelding(endringsmeldingId)
		val deltaker = deltakerService.hentDeltaker(endringsmelding.deltakerId)

		tiltaksansvarligAutoriseringService.verifiserTilgangTilEndringsmelding(navIdent)
		tiltaksansvarligAutoriseringService.verifiserTilgangTilGjennomforing(navIdent, deltaker.gjennomforingId)

		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		endringsmeldingService.markerSomFerdig(endringsmeldingId, navAnsatt.id)
	}

}
