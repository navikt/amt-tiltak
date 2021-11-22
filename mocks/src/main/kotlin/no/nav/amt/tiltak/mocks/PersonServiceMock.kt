package no.nav.amt.tiltak.mocks

import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.Kontaktinformasjon
import no.nav.amt.tiltak.core.port.Person
import no.nav.amt.tiltak.core.port.PersonService
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Primary
@Profile("local")
class PersonServiceMock : PersonService {

	override fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon {
		TODO("Not yet implemented")
	}

	override fun hentPerson(fnr: String): Person {
		return PersonMockDataProvider.getPersonByFodselsnummer(fnr)?.toPdlBruker()
			?: throw RuntimeException("Mock inneholder ikke informasjon om bruker med fødselsnummer $fnr")
	}

	override fun hentVeileder(fnr: String): Veileder? {
		throw NotImplementedError("Mock does not support getting Veileder")
	}

	private fun PersonMockData.toPdlBruker(): Person {
		return Person(
			fornavn = this.fornavn,
			mellomnavn = this.mellomnavn,
			etternavn = this.etternavn,
			telefonnummer = this.telefonnummer
		)
	}
}
