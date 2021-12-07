package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.arrangor.controllers.dto.AnsattDto
import no.nav.amt.tiltak.arrangor.controllers.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/arrangor/ansatt")
class AnsattController(
	private val authService: AuthService,
	private val service: ArrangorService
) {

	@Protected
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): AnsattDto {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		return service.getAnsattByPersonligIdent(personligIdent)
			.toDto()
	}

}
