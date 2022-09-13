package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/arrangor/ansatt", "/api/tiltaksarrangor/ansatt"])
class TiltakarrangorAnsattController(
	private val authService: AuthService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val personService: PersonService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): AnsattDto {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		return arrangorAnsattService.getAnsattByPersonligIdent(personligIdent)?.toDto() ?:
			personService.hentPerson(personligIdent).let {
				AnsattDto(
					fornavn = it.fornavn,
					etternavn = it.etternavn,
					arrangorer = emptyList()
				)
			}
	}
}
