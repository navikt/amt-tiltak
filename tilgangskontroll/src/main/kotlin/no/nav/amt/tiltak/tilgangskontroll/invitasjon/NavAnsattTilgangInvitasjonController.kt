package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/nav-ansatt/tilgang/invitasjon")
class NavAnsattTilgangInvitasjonController {

	@GetMapping("/ubrukt")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun hentUbrukteInvitasjoner(@RequestParam gjennomforingId: UUID) {

	}

	@PostMapping
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun opprettInvitasjon(@RequestBody request: OpprettInvitasjonRequest) {

	}

	@PatchMapping("/{invitasjonId}/avbryt")
	@ProtectedWithClaims(issuer = Issuer.AZURE_AD)
	fun avbrytInvitasjon(@PathVariable invitasjonId: UUID) {

	}

	data class OpprettInvitasjonRequest(
		val gjennomforingId: UUID
	)

}
