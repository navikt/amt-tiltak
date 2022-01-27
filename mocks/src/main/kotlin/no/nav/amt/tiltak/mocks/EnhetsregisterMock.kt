package no.nav.amt.tiltak.mocks

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Primary
@Profile("local")
class EnhetsregisterMock : EnhetsregisterClient {

	override fun hentVirksomhet(organisasjonsnummer: String): Virksomhet {
		val arrangor =
			ArrangorMockDataProvider.getArrangorByVirksomhetsnummer(organisasjonsnummer)
				?: throw IllegalArgumentException("Virksomhet med virksomhetsnummer $organisasjonsnummer eksisterer ikke")

		return Virksomhet(
			overordnetEnhetOrganisasjonsnummer = arrangor.organisasjonsnummer,
			overordnetEnhetNavn = arrangor.organisasjonsnavn,
			organisasjonsnummer = arrangor.virksomhetsnummer,
			navn = arrangor.virksomhetsnavn
		)
	}
}
