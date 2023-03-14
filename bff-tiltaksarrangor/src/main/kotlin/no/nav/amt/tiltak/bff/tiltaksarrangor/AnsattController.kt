package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
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
) {

	@ProtectedWithClaims(issuer = Issuer.TOKEN_X)
	@GetMapping("/meg/roller")
	fun getMineRoller(): List<String> {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()
		arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(personligIdent)

		val ansatt = arrangorAnsattService.getAnsattByPersonligIdent(personligIdent)

		val roller = ansatt?.arrangorer
			?.flatMap { it.roller }
			?: emptyList()

		if (ansatt != null && roller.isNotEmpty()) {
			arrangorAnsattService.setVellykketInnlogging(ansatt.id)
		}

		return roller.map { it.name }
	}
}
