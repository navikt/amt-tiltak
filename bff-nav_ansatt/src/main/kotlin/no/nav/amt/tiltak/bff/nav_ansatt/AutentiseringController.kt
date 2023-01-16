package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("AutentiseringControllerNavAnsatt")
@RequestMapping("/api/nav-ansatt/autentisering")
open class AutentiseringController(
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/meg")
	fun me(): MegDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val adGrupper = authService.hentAdGrupperTilInnloggetBruker()
		val veileder = navAnsattService.getNavAnsatt(navIdent)

		return MegDto(
			navIdent = veileder.navIdent,
			navn = veileder.navn,
			tilganger = adGrupper
				.mapNotNull(this::mapAdGruppeTilTilgang)
				.toSet()
		)
	}

	private fun mapAdGruppeTilTilgang(adGruppe: AuthService.AdGruppe): Tilgang? {
		return when(adGruppe) {
			AuthService.AdGruppe.TILTAKSANSVARLIG_FLATE_GRUPPE -> Tilgang.FLATE
			AuthService.AdGruppe.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE -> Tilgang.ENDRINGSMELDING
			AuthService.AdGruppe.TILTAKSANSVARLIG_EGNE_ANSATTE_GRUPPE -> Tilgang.EGNE_ANSATTE
			else -> null
		}
	}

	data class MegDto(
		val navIdent: String,
		val navn: String,
		val tilganger: Set<Tilgang>
	)

	enum class Tilgang {
		FLATE,
		ENDRINGSMELDING,
		EGNE_ANSATTE,
	}

}
