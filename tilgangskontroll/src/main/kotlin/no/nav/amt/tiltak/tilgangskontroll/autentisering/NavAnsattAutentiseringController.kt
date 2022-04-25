package no.nav.amt.tiltak.tilgangskontroll.autentisering

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/nav-ansatt/autentisering")
open class NavAnsattAutentiseringController(
	private val authService: AuthService,
	private val veilederService: VeilederService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/meg")
	fun me(): MegDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val veileder = veilederService.getOrCreateVeileder(navIdent)

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
