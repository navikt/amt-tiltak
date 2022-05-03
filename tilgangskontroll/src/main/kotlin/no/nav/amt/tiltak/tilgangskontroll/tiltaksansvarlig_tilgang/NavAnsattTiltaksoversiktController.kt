package no.nav.amt.tiltak.tilgangskontroll.tiltaksansvarlig_tilgang

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tiltaksoversikt")
class NavAnsattTiltaksoversiktController(
	private val authService: AuthService,
	private val navAnsattService: NavAnsattService,
	private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
	private val hentTiltaksoversiktQuery: HentTiltaksoversiktQuery
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping
	fun hentMinOversikt(): List<TiltaksoversiktGjennomforingDto> {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		val gjennomforingIder = tiltaksansvarligTilgangService.hentAktiveTilganger(navAnsatt.id)
			.map { it.gjennomforingId }

		return hentTiltaksoversiktQuery.query(gjennomforingIder)
			.map {
				TiltaksoversiktGjennomforingDto(
					id = it.id,
					navn = it.navn,
					arrangorNavn = it.arrangorOrganisasjonsnavn ?: it.arrangorVirksomhetsnavn
				)
			}
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping
	fun leggTilIMinOversikt(@RequestParam gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.giTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@DeleteMapping
	fun slettFraMinOversikt(@RequestParam gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.fjernTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

	data class TiltaksoversiktGjennomforingDto(
		val id: UUID,
		val navn: String,
		val arrangorNavn: String,
	)

}
