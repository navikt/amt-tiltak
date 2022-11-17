package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.ansatt.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("AnsattControllerTiltaksarrangor")
@RequestMapping("/api/tiltaksarrangor/ansatt")
class AnsattController(
	private val authService: AuthService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val personService: PersonService
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/meg")
	fun getInnloggetAnsatt(): no.nav.amt.tiltak.ansatt.AnsattDto {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(personligIdent)

		return arrangorAnsattService.getAnsattByPersonligIdent(personligIdent)?.toDto() ?:
			personService.hentPerson(personligIdent).let {
				no.nav.amt.tiltak.ansatt.AnsattDto(
					fornavn = it.fornavn,
					etternavn = it.etternavn,
					arrangorer = emptyList()
				)
			}
	}
}
