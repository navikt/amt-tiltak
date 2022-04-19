package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavKontorConnector
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.nav_kontor.NavKontorRepository
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.services.BrukerServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

class EndringPaaBrukerIngestorImplIntegrationTest {

	lateinit var endringPaaBrukerIngestorImpl: EndringPaaBrukerIngestor
	lateinit var brukerService: BrukerService

	lateinit var brukerRepository: BrukerRepository
	lateinit var navKontorRepository: NavKontorRepository

	val navKontorConnector: NavKontorConnector = mockk()
	val personService: PersonService = mockk()
	val veilederService: VeilederService = mockk()

	val norgClient:NorgClient = mockk()
	lateinit var dataSource: DataSource
	lateinit var jdbcTemplate: NamedParameterJdbcTemplate

	@BeforeEach
	fun beforeEach() {
		dataSource = SingletonPostgresContainer.getDataSource()
		jdbcTemplate = NamedParameterJdbcTemplate(dataSource)

		navKontorRepository = NavKontorRepository(jdbcTemplate)
		brukerRepository = BrukerRepository(jdbcTemplate)
		brukerService = BrukerServiceImpl(brukerRepository, navKontorRepository, navKontorConnector, personService, veilederService)
		endringPaaBrukerIngestorImpl = EndringPaaBrukerIngestorImpl(brukerService, norgClient)
	}

	@Test
	fun `ingestKafkaRecord - bruker finnes, har ikke nav kontor - oppdatere nav kontor`() {
		val fnr = "121234324"
		val expectedNyEnhet = "enhet2"
		val expectedNyttKontorNavn = "Nytt navkontor navn"

		val bruker = BrukerInsertDbo(
			fodselsnummer = fnr,
			fornavn = "person.fornavn",
			mellomnavn = null,
			etternavn = "person.etternavn",
			telefonnummer = null,
			epost = null,
			ansvarligVeilederId = null,
			navKontorId = null
		)

		brukerRepository.insert(bruker)

		every { norgClient.hentNavKontorNavn(expectedNyEnhet) }.returns(expectedNyttKontorNavn)

		endringPaaBrukerIngestorImpl.ingestKafkaRecord("""
			{
				"fodselsnummer": "$fnr",
				"oppfolgingsenhet": "$expectedNyEnhet"
			}
		""".trimIndent())

		val insertedBruker = brukerRepository.get(fnr)
		val navKontorId = insertedBruker?.navKontorId
		insertedBruker shouldNotBe null
		navKontorId shouldNotBe null

		val navkontor = navKontorRepository.get(navKontorId!!)

		navkontor.navn shouldBe expectedNyttKontorNavn
		navkontor.enhetId shouldBe expectedNyEnhet

	}
}
