package no.nav.amt.tiltak.tilgangskontroll.tiltaksansvarlig_tilgang

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tiltaksansvarlig/gjennomforing-tilgang")
class TiltaksansvarligGjennomforingTilgangController(
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
    private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping
	fun giTilgangTilGjennomforing(@RequestParam gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getOrCreateNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.giTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PatchMapping("/stop")
	fun stopTilgangTilGjennomforing(@RequestParam("gjennomforingId") gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getOrCreateNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.stopTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

}
