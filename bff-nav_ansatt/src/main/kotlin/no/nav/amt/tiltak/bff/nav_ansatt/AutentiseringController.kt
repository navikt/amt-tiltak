package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.common.auth.AdGruppe
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
				.map(this::mapAdGruppeTilTilgang)
				.toSet()
		)
	}

	private fun mapAdGruppeTilTilgang(adGruppe: AdGruppe): Tilgang {
		return when(adGruppe) {
			AdGruppe.TILTAKSANSVARLIG_FLATE_GRUPPE -> Tilgang.FLATE
			AdGruppe.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE -> Tilgang.ENDRINGSMELDING
			AdGruppe.TILTAKSANSVARLIG_EGNE_ANSATTE_GRUPPE -> Tilgang.EGNE_ANSATTE
			AdGruppe.TILTAKSANSVARLIG_FORTROLIG_ADRESSE_GRUPPE -> Tilgang.FORTROLIG_ADRESSE
			AdGruppe.TILTAKSANSVARLIG_STRENGT_FORTROLIG_ADRESSE_GRUPPE -> Tilgang.STRENGT_FORTROLIG_ADRESSE
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
		FORTROLIG_ADRESSE,
		STRENGT_FORTROLIG_ADRESSE,
	}

}
