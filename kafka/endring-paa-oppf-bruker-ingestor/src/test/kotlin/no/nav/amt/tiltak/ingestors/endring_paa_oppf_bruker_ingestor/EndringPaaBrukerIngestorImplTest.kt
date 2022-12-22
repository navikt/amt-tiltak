package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class EndringPaaBrukerIngestorImplTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var deltakerService: DeltakerService
	lateinit var navEnhetService: NavEnhetService

	val fnr = "121234324"
	val navEnhet = NavEnhet(UUID.randomUUID(),"enhet", "Navn")
	val deltaker = Deltaker(
		id = UUID.randomUUID(),
		gjennomforingId = UUID.randomUUID(),
		fornavn = "fornavn",
		mellomnavn = null,
		etternavn = "etternavn",
		fodselsnummer = fnr,
		telefonnummer = "1234",
		epost = "foo@bar.baz",
		navVeilederId = UUID.randomUUID(),
		navEnhetId = navEnhet.id,
		startDato = null,
		sluttDato = null,
		status = DeltakerStatus(
			id = UUID.randomUUID(),
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now(),
			opprettetDato = LocalDateTime.now(),
			aktiv = true,
		),
		registrertDato = LocalDateTime.now(),
		dagerPerUke = null,
		prosentStilling = null,
		innsokBegrunnelse = null,
		erSkjermet = false
	)


	@BeforeEach
	fun beforeEach() {
		navEnhetService = mockk()
		deltakerService = mockk()
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(deltakerService, navEnhetService)
	}

	@Test
	fun `ingestKafkaRecord - bruker finnes ikke - skal returnere med en gang`() {
		val enhet = "enhet"
		every { deltakerService.hentDeltakereMedFnr(fnr) }.returns(emptyList())
		every { navEnhetService.getNavEnhet(navEnhet.id) }.returns(navEnhet)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$enhet"
			}
		""".trimIndent())

		verify ( exactly = 0 ) { navEnhetService.getNavEnhet(enhet) }
		verify ( exactly = 0 ) { deltakerService.oppdaterNavEnhet(fnr, any()) }

	}

	@Test
	fun `ingestKafkaRecord - samme nav enhet - skal returnere med en gang`() {
		val enhet = "enhet"

		every { deltakerService.hentDeltakereMedFnr(fnr) }.returns(listOf(deltaker))
		every { navEnhetService.getNavEnhet(navEnhet.id) }.returns(navEnhet)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$enhet"
			}
		""".trimIndent())

		verify ( exactly = 0 ) { navEnhetService.getNavEnhet(enhet) }
		verify ( exactly = 0 ) { deltakerService.oppdaterNavEnhet(fnr, any()) }
	}

	@Test
	fun `ingestKafkaRecord - endret nav enhet - oppdaterer nav enhet`() {
		val nyEnhet = "enhet2"
		val nyttEnhetNavn = "Nytt nav enhet navn"
		every { deltakerService.hentDeltakereMedFnr(fnr) }.returns(listOf(deltaker))
		every { navEnhetService.getNavEnhet(navEnhet.id) }.returns(navEnhet)
		every { navEnhetService.getNavEnhet(nyEnhet)}.returns(NavEnhet(UUID.randomUUID(), nyEnhet, nyttEnhetNavn))
		every { deltakerService.oppdaterNavEnhet(fnr, any())}.returns(Unit)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$nyEnhet"
			}
		""".trimIndent())

		verify ( exactly = 1 ) { navEnhetService.getNavEnhet(nyEnhet) }
		verify ( exactly = 1 ) { deltakerService.oppdaterNavEnhet(fnr, any() ) }
	}

	@Test
	fun `ingestKafkaRecord - har nav enhet, fjernes på topic - endrer ikke nav enhet`() {
		//Det er ikke mulig å fjerne nav kontor i arena men det kan legges meldinger på topicen som endrer andre ting
		//og derfor ikke er relevante
		val nyEnhet = "enhet2"

		every { deltakerService.hentDeltakereMedFnr(fnr) }.returns(listOf(deltaker))
		every { navEnhetService.getNavEnhet(navEnhet.id) }.returns(navEnhet)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": null
			}
		""".trimIndent())

		verify ( exactly = 0 ) { navEnhetService.getNavEnhet(nyEnhet) }
		verify ( exactly = 0 ) { deltakerService.oppdaterNavEnhet(fnr, any() ) }
	}
}
