package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.nav_enhet.NavEnhetRepository
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertNavEnhetCommand
import no.nav.amt.tiltak.tiltak.services.BrukerServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*
import javax.sql.DataSource

class EndringPaaBrukerIngestorImplIntegrationTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var brukerService: BrukerService

	lateinit var brukerRepository: BrukerRepository
	lateinit var navEnhetRepository: NavEnhetRepository

	val personService: PersonService = mockk()
	val navAnsattService: NavAnsattService = mockk()
	val navEnhetService: NavEnhetService = mockk()

	lateinit var dataSource: DataSource
	lateinit var jdbcTemplate: NamedParameterJdbcTemplate
	lateinit var testDataRepository: TestDataRepository

	@BeforeEach
	fun beforeEach() {
		dataSource = SingletonPostgresContainer.getDataSource()
		jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
		testDataRepository = TestDataRepository(jdbcTemplate)

		navEnhetRepository = NavEnhetRepository(jdbcTemplate)
		brukerRepository = BrukerRepository(jdbcTemplate)
		brukerService = BrukerServiceImpl(brukerRepository, personService, navAnsattService, navEnhetService)
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(brukerService, navEnhetService)
	}

	@Test
	fun `ingestKafkaRecord - bruker finnes, har ikke nav kontor - oppdatere nav kontor`() {
		val fnr = "121234324"
		val expectedNyEnhet = "enhet2"
		val expectedNyttEnhetNavn = "Nytt nav enhet navn"
		val navEnhet = NavEnhet(UUID.randomUUID(), expectedNyEnhet, expectedNyttEnhetNavn)

		val bruker = BrukerInsertDbo(
			fodselsnummer = fnr,
			fornavn = "person.fornavn",
			mellomnavn = null,
			etternavn = "person.etternavn",
			telefonnummer = null,
			epost = null,
			ansvarligVeilederId = null,
			navEnhetId = null
		)

		testDataRepository.insertNavEnhet(InsertNavEnhetCommand(navEnhet.id, navEnhet.enhetId, navEnhet.navn))
		brukerRepository.insert(bruker)

		every { navEnhetService.getNavEnhet(expectedNyEnhet) }.returns(
			navEnhet
		)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$expectedNyEnhet"
			}
		""".trimIndent())

		val insertedBruker = brukerRepository.get(fnr)
		val navEnhetId = insertedBruker?.navEnhetId
		insertedBruker shouldNotBe null
		navEnhetId shouldNotBe null

	}
}
