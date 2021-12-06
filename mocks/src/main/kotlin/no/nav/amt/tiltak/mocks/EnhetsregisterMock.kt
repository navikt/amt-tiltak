package no.nav.amt.tiltak.mocks

import no.nav.amt.tiltak.core.domain.enhetsregister.Virksomhet
import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Primary
@Profile("local")
class EnhetsregisterMock : EnhetsregisterConnector {

	override fun hentVirksomhet(virksomhetsnummer: String): Virksomhet {
		val tiltaksarrangor =
			TiltaksarrangorMockDataProvider.getTiltaksarrangorByVirksomhetsnummer(virksomhetsnummer)
				?: throw IllegalArgumentException("Virksomhet med virksomhetsnummer $virksomhetsnummer eksisterer ikke")

		return Virksomhet(
			overordnetEnhetOrganisasjonsnummer = tiltaksarrangor.organisasjonsnummer,
			overordnetEnhetNavn = tiltaksarrangor.organisasjonsnavn,
			organisasjonsnummer = tiltaksarrangor.virksomhetsnummer,
			navn = tiltaksarrangor.virksomhetsnavn
		)
	}
}
