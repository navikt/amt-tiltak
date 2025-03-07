package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.TiltaksansvarligTilgangService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("TilgangAPINavAnsatt")
@RequestMapping("/api/nav-ansatt/gjennomforing-tilgang")
class TilgangAPI(
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
    private val tiltaksansvarligTilgangService: TiltaksansvarligTilgangService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PostMapping
	fun giTilgangTilGjennomforing(@RequestParam gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.giTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@PatchMapping("/stop")
	fun stopTilgangTilGjennomforing(@RequestParam("gjennomforingId") gjennomforingId: UUID){
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val navAnsatt = navAnsattService.getNavAnsatt(navIdent)

		tiltaksansvarligTilgangService.stopTilgangTilGjennomforing(navAnsatt.id, gjennomforingId)
	}

}
