package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tiltaksarrangor/tilgang/invitasjon")
class TiltaksarrangorTilgangInvitasjonController {

	@GetMapping("/{invitasjonId}/info")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun hentInvitasjonInfo(@PathVariable invitasjonId: UUID) {

	}

	@PatchMapping("/{invitasjonId}/aksepter")
	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	fun aksepterInvitasjon(@PathVariable invitasjonId: UUID) {

	}

}
