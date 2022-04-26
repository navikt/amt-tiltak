package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavKontorService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class EndringPaaBrukerIngestorImplTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var brukerService: BrukerService
	lateinit var navKontorService: NavKontorService


	@BeforeEach
	fun beforeEach() {
		navKontorService = mockk()
		brukerService = mockk()
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(brukerService, navKontorService)
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

		verify ( exactly = 0 ) { navKontorService.getNavKontor(enhet) }
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
			fodselsnummer = fnr,
			navKontor = NavKontor(UUID.randomUUID(), enhet, "Navn")
		)
		every { brukerService.getBruker(fnr) }.returns(bruker)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$enhet"
			}
		""".trimIndent())

		verify ( exactly = 0 ) { navKontorService.getNavKontor(enhet) }
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
			fodselsnummer = fnr,
			navKontor = NavKontor(UUID.randomUUID(),"enhet", "Navn")
		)

		every { brukerService.getBruker(fnr) }.returns(bruker)
		every { navKontorService.getNavKontor(nyEnhet)}.returns(NavKontor(UUID.randomUUID(), nyEnhet, nyttKontorNavn))
		every { brukerService.oppdaterNavKontor(fnr, any())}.returns(Unit)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$nyEnhet"
			}
		""".trimIndent())

		verify ( exactly = 1 ) { navKontorService.getNavKontor(nyEnhet) }
		verify ( exactly = 1 ) { brukerService.oppdaterNavKontor(fnr, any() ) }
	}

	@Test
	fun `ingestKafkaRecord - har nav kontor, fjernes på topic - endrer ikke nav kontor`() {
		//Det er ikke mulig å fjerne nav kontor i arena men det kan legges meldinger på topicen som endrer andre ting
		//og derfor ikke er relevante
		val fnr = "121234324"
		val nyEnhet = "enhet2"
		val bruker = Bruker(
			id = UUID.randomUUID(),
			fornavn = "fornavn",
			mellomnavn = null,
			etternavn = "etternavn",
			fodselsnummer = fnr,
			navKontor = NavKontor(UUID.randomUUID(),"enhet", "Navn")
		)
		every { brukerService.getBruker(fnr) }.returns(bruker)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": null
			}
		""".trimIndent())

		verify ( exactly = 0 ) { navKontorService.getNavKontor(nyEnhet) }
		verify ( exactly = 0 ) { brukerService.oppdaterNavKontor(fnr, any() ) }
	}
}
