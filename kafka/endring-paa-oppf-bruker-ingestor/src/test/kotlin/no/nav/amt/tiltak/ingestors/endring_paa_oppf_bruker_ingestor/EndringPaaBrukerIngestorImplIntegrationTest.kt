package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.nav_enhet.NavEnhetRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import no.nav.amt.tiltak.test.database.data.inputs.NavEnhetInput
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.deltaker.service.BrukerService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.sql.DataSource

class EndringPaaBrukerIngestorImplIntegrationTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var deltakerService: DeltakerService
	lateinit var brukerService: BrukerService

	lateinit var brukerRepository: BrukerRepository
	lateinit var navEnhetRepository: NavEnhetRepository
	lateinit var deltakerRepository: DeltakerRepository
	lateinit var deltakerStatusRepository: DeltakerStatusRepository
	lateinit var gjennomforingRepository: GjennomforingRepository

	val personService: PersonService = mockk()
	val navAnsattService: NavAnsattService = mockk()
	val navEnhetService: NavEnhetService = mockk()
	val endringsmeldingService: EndringsmeldingService = mockk()
	val skjermetPersonService: SkjermetPersonService = mockk()

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
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		gjennomforingRepository = GjennomforingRepository(jdbcTemplate)

		brukerService = BrukerService(brukerRepository, personService, navAnsattService, navEnhetService, skjermetPersonService)
		deltakerService = DeltakerServiceImpl(
			deltakerRepository,
			deltakerStatusRepository,
			brukerService,
			endringsmeldingService,
			mockk(),
			TransactionTemplate(),
			mockk()
		)
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(deltakerService, navEnhetService)
	}

	@Test
	fun `ingestKafkaRecord - bruker finnes, har ikke nav kontor - oppdatere nav kontor`() {
		val expectedNyEnhet = "enhet2"
		val expectedNyttEnhetNavn = "Nytt nav enhet navn"
		val navEnhet = NavEnhet(UUID.randomUUID(), expectedNyEnhet, expectedNyttEnhetNavn)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertDefaultTestData)
		testDataRepository.insertNavEnhet(NavEnhetInput(navEnhet.id, navEnhet.enhetId, navEnhet.navn))

		every {
			navEnhetService.getNavEnhet(NAV_ENHET_1.id)
		} returns NAV_ENHET_1.toNavEnhet()

		every {
			navEnhetService.getNavEnhet(expectedNyEnhet)
		} returns navEnhet

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "${BRUKER_1.personIdent}",
				"oppfolgingsenhet": "$expectedNyEnhet"
			}
		""".trimIndent())

		val insertedBruker = brukerRepository.get(BRUKER_1.personIdent)
		val navEnhetId = insertedBruker?.navEnhetId
		insertedBruker shouldNotBe null
		navEnhetId shouldBe navEnhet.id

	}
}
