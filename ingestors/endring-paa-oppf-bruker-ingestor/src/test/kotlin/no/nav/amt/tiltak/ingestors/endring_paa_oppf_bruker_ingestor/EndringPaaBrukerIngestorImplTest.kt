package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.mockk.*
import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor.EndringPaaBrukerIngestorImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class EndringPaaBrukerIngestorImplTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var brukerService: BrukerService
	lateinit var norgClient: NorgClient


	@BeforeEach
	fun beforeEach() {
		norgClient = mockk()
		brukerService = mockk()
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(brukerService, norgClient)
	}

	@Test
	fun `ingestKafkaRecord - bruker finnes ikke - skal returnere med en gang`() {
		val fnr = "121234324"
		val enhet = "enhet"
		every { brukerService.getBruker(fnr) }.returns(null)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$enhet"
			}
		""".trimIndent())

		verify ( exactly = 0 ) { norgClient.hentNavKontorNavn(enhet) }
		verify ( exactly = 0 ) { brukerService.oppdaterNavKontor(fnr, any()) }

	}

	@Test
	fun `ingestKafkaRecord - samme nav kontor - skal returnere med en gang`() {
		val fnr = "121234324"
		val enhet = "enhet"
		val bruker = Bruker(
			id = UUID.randomUUID(),
			fornavn = "fornavn",
			mellomnavn = null,
			etternavn = "etternavn",
			fodselsnummer = "$fnr",
			navKontor = NavKontor(enhet, "Navn")
		)
		every { brukerService.getBruker(fnr) }.returns(bruker)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$enhet"
			}
		""".trimIndent())

		verify ( exactly = 0 ) { norgClient.hentNavKontorNavn(enhet) }
		verify ( exactly = 0 ) { brukerService.oppdaterNavKontor(fnr, any()) }
	}

	@Test
	fun `ingestKafkaRecord - endret nav kontor - oppdaterer nav kontor`() {
		val fnr = "121234324"
		val nyEnhet = "enhet2"
		val nyttKontorNavn = "Nytt navkontor navn"
		val bruker = Bruker(
			id = UUID.randomUUID(),
			fornavn = "fornavn",
			mellomnavn = null,
			etternavn = "etternavn",
			fodselsnummer = "$fnr",
			navKontor = NavKontor("enhet", "Navn")
		)
		val navKontor = NavKontor(nyEnhet, nyttKontorNavn)
		every { brukerService.getBruker(fnr) }.returns(bruker)
		every { norgClient.hentNavKontorNavn(nyEnhet)}.returns(nyttKontorNavn)
		every { brukerService.oppdaterNavKontor(fnr, navKontor)}.returns(Unit)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$nyEnhet"
			}
		""".trimIndent())

		verify ( exactly = 1 ) { norgClient.hentNavKontorNavn(nyEnhet) }
		verify ( exactly = 1 ) { brukerService.oppdaterNavKontor(fnr, navKontor ) }
	}
}
