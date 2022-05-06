package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/arrangor-ansatt-tilgang/foresporsel")
class NavAnsattArrangorAnsattTilgangForesporselController(
    private val tilgangForesporselService: TilgangForesporselService,
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
    private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService
) {

	@GetMapping("/ubesluttet")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbesluttedeForesporsler(@RequestParam gjennomforingId: UUID): List<UbesluttetForesporselDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		verifisierTilgangTilGjennomforing(navIdent, gjennomforingId)

		return tilgangForesporselService.hentUbesluttedeForesporsler(gjennomforingId)
			.map { tilDto(it) }
	}

	@PatchMapping("/{foresporselId}/godkjenn")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun godkjennForesporsel(@PathVariable foresporselId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)
		val foresporsel = tilgangForesporselService.hentForesporsel(foresporselId)

		verifisierTilgangTilGjennomforing(navIdent, foresporsel.gjennomforingId)

		tilgangForesporselService.godkjennForesporsel(foresporselId, navAnsatt.id)
	}

	@PatchMapping("/{foresporselId}/avvis")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun avvisForesporsel(@PathVariable foresporselId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)
		val foresporsel = tilgangForesporselService.hentForesporsel(foresporselId)

		verifisierTilgangTilGjennomforing(navIdent, foresporsel.gjennomforingId)

		tilgangForesporselService.avvisForesporsel(foresporselId, navAnsatt.id)
	}

	data class UbesluttetForesporselDto(
		val id: UUID,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val fodselsnummer: String,
		val opprettetDato: ZonedDateTime,
	)

	private fun tilDto(dbo: TilgangForesporselDbo): UbesluttetForesporselDto {
		return UbesluttetForesporselDto(
			id = dbo.id,
			fornavn = dbo.fornavn,
			mellomnavn = dbo.mellomnavn,
			etternavn = dbo.etternavn,
			fodselsnummer = dbo.personligIdent,
			opprettetDato = dbo.createdAt,
		)
	}

	private fun verifisierTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID) {
		if (!tiltaksansvarligTilgangService.harTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Har ikke tilgang til gjennomføring")
		}
	}

}
