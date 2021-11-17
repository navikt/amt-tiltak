package no.nav.amt.tiltak.connectors.person

import no.nav.amt.tiltak.connectors.dkif.DkifConnector
import no.nav.amt.tiltak.connectors.pdl.PdlConnector
import no.nav.amt.tiltak.core.port.Kontaktinformasjon
import no.nav.amt.tiltak.core.port.Person
import no.nav.amt.tiltak.core.port.PersonService
import org.springframework.stereotype.Service

@Service
class PersonFacade(
	private val pdlConnector: PdlConnector,
	private val dkifConnector: DkifConnector,
) : PersonService {

	override fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val kontaktinformasjon = dkifConnector.hentBrukerKontaktinformasjon(fnr)

		return Kontaktinformasjon(
			epost = kontaktinformasjon.epost,
			telefonnummer = kontaktinformasjon.telefonnummer
		)
	}

	override fun hentPerson(fnr: String): Person {
		val bruker = pdlConnector.hentBruker(fnr)

		return Person(
			fornavn = bruker.fornavn,
			mellomnavn = bruker.mellomnavn,
			etternavn = bruker.etternavn,
			telefonnummer = bruker.telefonnummer
		)
	}

}
