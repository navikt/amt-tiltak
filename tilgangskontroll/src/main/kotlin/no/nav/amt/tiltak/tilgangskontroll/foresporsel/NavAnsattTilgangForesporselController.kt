package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tilgang/foresporsel")
class NavAnsattTilgangForesporselController {

	@GetMapping("/ubesluttet")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbesluttedeForesporsler(@RequestParam gjennomforingId: UUID) {

	}

	@PatchMapping("/{foresporselId}/godkjenn")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun godkjennForesporsel(@PathVariable foresporselId: UUID) {

	}

	@PatchMapping("/{foresporselId}/avvis")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun avvisForesporsel(@PathVariable foresporselId: UUID) {

	}

}
