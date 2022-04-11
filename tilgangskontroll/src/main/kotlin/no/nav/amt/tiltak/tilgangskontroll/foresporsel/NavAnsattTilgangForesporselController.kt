package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tilgang/foresporsel")
class NavAnsattTilgangForesporselController(
	private val tilgangForesporselService: TilgangForesporselService,
	private val authService: AuthService,
	private val veilederService: VeilederService
) {

	@GetMapping("/ubesluttet")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbesluttedeForesporsler(@RequestParam gjennomforingId: UUID): List<UbesluttetForesporselDto> {
		// Mangler tilgangskontroll på nav enhet

		return tilgangForesporselService.hentUbesluttedeForesporsler(gjennomforingId)
			.map { tilDto(it) }
	}

	@PatchMapping("/{foresporselId}/godkjenn")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun godkjennForesporsel(@PathVariable foresporselId: UUID) {
		// Mangler tilgangskontroll på nav enhet

		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = veilederService.getOrCreateVeileder(navIdent)

		tilgangForesporselService.godkjennForesporsel(foresporselId, navAnsatt.id)
	}

	@PatchMapping("/{foresporselId}/avvis")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun avvisForesporsel(@PathVariable foresporselId: UUID) {
		// Mangler tilgangskontroll på nav enhet

		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = veilederService.getOrCreateVeileder(navIdent)

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

}
