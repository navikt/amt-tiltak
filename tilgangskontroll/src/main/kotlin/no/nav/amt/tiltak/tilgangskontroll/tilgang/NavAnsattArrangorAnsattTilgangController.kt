package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattTilgangService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/arrangor-ansatt-tilgang")
class NavAnsattArrangorAnsattTilgangController(
	private val authService: AuthService,
	private val navAnsattTilgangService: NavAnsattTilgangService,
	private val veilederService: VeilederService,
	private val hentArrangorAnsattTilgangerQuery: HentArrangorAnsattTilgangerQuery,
	private val gjennomforingTilgangService: GjennomforingTilgangService
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentTilganger(@RequestParam("gjennomforingId") gjennomforingId: UUID): List<TilgangDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()

		verifisierTilgangTilGjennomforing(navIdent, gjennomforingId)

		return hentArrangorAnsattTilgangerQuery.query(gjennomforingId)
			.map {
				TilgangDto(
					id = it.id,
					fornavn = it.fornavn,
					mellomnavn = it.mellomnavn,
					etternavn = it.etternavn,
					opprettetDato = it.opprettetDato,
					opprettetAvNavIdent = it.opprettetAvNavIdent ?: "Ukjent",
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PatchMapping("/{tilgangId}/stop")
	fun stopTilgang(@PathVariable("tilgangId") tilgangId: UUID) {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val tilgang = gjennomforingTilgangService.hentTilgang(tilgangId)

		verifisierTilgangTilGjennomforing(navIdent, tilgang.gjennomforingId)

		val navAnsatt = veilederService.getOrCreateVeileder(navIdent)

		gjennomforingTilgangService.stopTilgang(tilgang.id, navAnsatt.id)
	}

	data class TilgangDto(
		val id: UUID,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val opprettetDato: ZonedDateTime,
		val opprettetAvNavIdent: String
	)

	private fun verifisierTilgangTilGjennomforing(navIdent: String, gjennomforingId: UUID) {
		if (!navAnsattTilgangService.harTiltaksansvarligTilgangTilGjennomforing(navIdent, gjennomforingId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Har ikke tilgang til gjennomføring")
		}
	}

}
