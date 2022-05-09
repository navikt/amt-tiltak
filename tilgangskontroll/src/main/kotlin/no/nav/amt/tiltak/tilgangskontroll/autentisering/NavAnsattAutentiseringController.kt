package no.nav.amt.tiltak.tilgangskontroll.autentisering

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/nav-ansatt/autentisering")
open class NavAnsattAutentiseringController(
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/meg")
	fun me(): MegDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val veileder = navAnsattService.getNavAnsatt(navIdent)

		return MegDto(
			navIdent = veileder.navIdent,
			navn = veileder.navn
		)
	}

	data class MegDto(
		val navIdent: String,
		val navn: String,
	)

}
