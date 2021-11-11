package no.nav.amt.tiltak.mocks

import no.nav.amt.tiltak.core.port.PdlBruker
import no.nav.amt.tiltak.core.port.PdlConnector
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Primary
@Profile("local")
class PdlConnectorMock : PdlConnector {

	override fun hentBruker(brukerFnr: String): PdlBruker {
		return PersonMockDataProvider.getPersonByFodselsnummer(brukerFnr)?.toPdlBruker()
			?: throw RuntimeException("Mock inneholder ikke informasjon om bruker med fødselsnummer $brukerFnr")
	}

	private fun PersonMockData.toPdlBruker(): PdlBruker {
		return PdlBruker(
			fornavn = this.fornavn,
			mellomnavn = this.mellomnavn,
			etternavn = this.etternavn,
			telefonnummer = this.telefonnummer
		)
	}
}
