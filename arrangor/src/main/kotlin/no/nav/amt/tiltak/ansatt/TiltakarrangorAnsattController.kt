package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = [ "/api/arrangor/ansatt", "/api/tiltakarrangor/ansatt" ])
class TiltakarrangorAnsattController(
	private val authService: AuthService,
	private val service: ArrangorService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): AnsattDto {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		return service.getAnsattByPersonligIdent(personligIdent)
			.toDto()
	}

}
