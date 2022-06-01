package no.nav.amt.tiltak.tilgangskontroll.autentisering

import no.nav.amt.tiltak.clients.poao_tilgang.AdGruppe
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGruppeService
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGrupper.TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/nav-ansatt/autentisering")
open class NavAnsattAutentiseringController(
    private val authService: AuthService,
    private val navAnsattService: NavAnsattService,
	private val adGruppeService: AdGruppeService
) {

	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	@GetMapping("/meg")
	fun me(): MegDto {
		val navIdent = authService.hentNavIdentTilInnloggetBruker()
		val veileder = navAnsattService.getNavAnsatt(navIdent)
		val adGrupper = adGruppeService.hentAdGrupper(navIdent)

		return MegDto(
			navIdent = veileder.navIdent,
			navn = veileder.navn,
			tilganger = adGrupper
				.mapNotNull(this::mapAdGruppeTilTilgang)
				.toSet()
		)
	}

	private fun mapAdGruppeTilTilgang(adGruppe: AdGruppe): Tilgang? {
		return when(adGruppe.name) {
			TILTAKSANSVARLIG_FLATE_GRUPPE -> Tilgang.FLATE
			TILTAKSANSVARLIG_ENDRINGSMELDING_GRUPPE -> Tilgang.ENDRINGSMELDING
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
		ENDRINGSMELDING
	}

}
