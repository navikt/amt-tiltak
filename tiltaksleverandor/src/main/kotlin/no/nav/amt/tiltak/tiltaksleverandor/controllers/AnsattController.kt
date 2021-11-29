package no.nav.amt.tiltak.tiltaksleverandor.controllers

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.port.TiltaksleverandorService
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattDTO
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.toDto
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tiltaksleverandor/ansatt")
class AnsattController(
	private val authService: AuthService,
	private val service: TiltaksleverandorService
) {

	@Protected
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): AnsattDTO {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		return service.getAnsattByPersonligIdent(personligIdent)
			.toDto()
	}

}
