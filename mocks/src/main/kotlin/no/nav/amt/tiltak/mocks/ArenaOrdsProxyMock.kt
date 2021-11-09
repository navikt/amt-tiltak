package no.nav.amt.tiltak.mocks

import no.nav.amt.tiltak.core.port.Arbeidsgiver
import no.nav.amt.tiltak.core.port.ArenaOrdsProxyConnector
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Primary
@Profile("local")
class ArenaOrdsProxyMock : ArenaOrdsProxyConnector {

	override fun hentFnr(arenaPersonId: String): String? {
		return PersonMockDataProvider.getPersonByArenaId(arenaPersonId.toLong())?.fodselsnummer
	}

	override fun hentArbeidsgiver(arenaArbeidsgiverId: String): Arbeidsgiver? {
		TODO("Not yet implemented")
	}

	override fun hentVirksomhetsnummer(virksomhetsnummer: String): String {
		TODO("Not yet implemented")
	}
}
