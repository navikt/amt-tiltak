package no.nav.amt.tiltak.bff.tiltaksarrangor

import no.nav.amt.tiltak.ansatt.AnsattDto
import no.nav.amt.tiltak.ansatt.toDto
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
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
	fun getInnloggetAnsatt(): AnsattDto {
		val personligIdent = authService.hentPersonligIdentTilInnloggetBruker()

		arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(personligIdent)

		val ansatt = arrangorAnsattService.getAnsattByPersonligIdent(personligIdent)

		if (ansatt == null) {
			return personService.hentPerson(personligIdent).let {
				AnsattDto(
					fornavn = it.fornavn,
					etternavn = it.etternavn,
					arrangorer = emptyList()
				)
			}
		}

		val arrangorerForAnsattMedKoordinatorRolle = ansatt.arrangorer
			.filter { it.roller.contains(ArrangorAnsattRolle.KOORDINATOR) }

		if (arrangorerForAnsattMedKoordinatorRolle.isNotEmpty()) {
			arrangorAnsattService.setVellykketInnlogging(ansatt.id)
		}

		return AnsattDto(
			fornavn = ansatt.fornavn,
			etternavn = ansatt.etternavn,
			arrangorer = arrangorerForAnsattMedKoordinatorRolle.map { it.toDto() }
		)
	}

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
