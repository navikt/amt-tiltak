package no.nav.amt.tiltak.bff.tiltaksarrangor

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.amt.tiltak.arrangor.ArrangorRepository
import no.nav.amt.tiltak.arrangor.ArrangorServiceImpl
import no.nav.amt.tiltak.common.auth.AuthService
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestData.createBrukerInput
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestData.createGjennomforingInput
import no.nav.amt.tiltak.test.database.data.TestData.createStatusInput
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.HentKoordinatorerForGjennomforingQuery
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.services.BrukerService
import no.nav.amt.tiltak.tiltak.services.GjennomforingServiceImpl
import no.nav.amt.tiltak.tiltak.services.TiltakServiceImpl
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.mock
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GjennomforingControllerIntegrationTest {

	private val dataSource = SingletonPostgresContainer.getDataSource()

	private lateinit var namedJdbcTemplate: NamedParameterJdbcTemplate
	private lateinit var tiltakRepository: TiltakRepository
	private lateinit var deltakerRepository: DeltakerRepository
	private lateinit var brukerRepository: BrukerRepository
	private lateinit var brukerService: BrukerService
	private lateinit var deltakerStatusRepository: DeltakerStatusRepository
	private lateinit var gjennomforingRepository: GjennomforingRepository
	private lateinit var gjennomforingService: GjennomforingService
	private lateinit var deltakerService: DeltakerService
	private lateinit var arrangorService: ArrangorService
	private lateinit var endringsmeldingService: EndringsmeldingService
	private lateinit var endringsmeldingRepository: EndringsmeldingRepository
	private lateinit var auditLoggerService: AuditLoggerService
	private lateinit var authService: AuthService
	private lateinit var controller: GjennomforingController
	private lateinit var controllerService: ControllerService
	private lateinit var testDataRepository: TestDataRepository
	private lateinit var navEnhetService: NavEnhetService

	@BeforeEach
	fun before() {
		val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

		namedJdbcTemplate = NamedParameterJdbcTemplate(dataSource)
		gjennomforingRepository = GjennomforingRepository(namedJdbcTemplate)
		tiltakRepository = TiltakRepository(namedJdbcTemplate)
		deltakerRepository = DeltakerRepository(namedJdbcTemplate)
		brukerRepository = BrukerRepository(namedJdbcTemplate)
		endringsmeldingRepository = EndringsmeldingRepository(namedJdbcTemplate, transactionTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(namedJdbcTemplate)
		authService = mock(AuthService::class.java)
		auditLoggerService = mock(AuditLoggerService::class.java)
		navEnhetService = mock(NavEnhetService::class.java)

		arrangorService = ArrangorServiceImpl(mockk(), ArrangorRepository(namedJdbcTemplate))

		brukerService = BrukerService(
			brukerRepository,
			mock(PersonService::class.java),
			mock(NavAnsattService::class.java),
			mock(NavEnhetService::class.java)
		)

		deltakerService = DeltakerServiceImpl(
			deltakerRepository,
			deltakerStatusRepository,
			brukerService,
			transactionTemplate
		)

		gjennomforingService = GjennomforingServiceImpl(
			gjennomforingRepository,
			TiltakServiceImpl(tiltakRepository),
			deltakerService,
			arrangorService,
			transactionTemplate,
			HentKoordinatorerForGjennomforingQuery(namedJdbcTemplate)
		)

		endringsmeldingService = EndringsmeldingServiceImpl(
			endringsmeldingRepository = endringsmeldingRepository,
			auditLoggerService = auditLoggerService,
		)

		controllerService = ControllerService(
			deltakerService,
			mockk(),
			gjennomforingService,
			mockk(),
			mockk(),
		)

		controller = GjennomforingController(
			gjennomforingService, deltakerService,
			authService, mock(ArrangorAnsattTilgangService::class.java),
			endringsmeldingService,
		)

		testDataRepository = TestDataRepository(namedJdbcTemplate)
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
	}

	@Test
	fun `hentGjennomforing - tiltaksgjennomføring finnes ikke - skal returnere NOT FOUND`() {
		val id = UUID.randomUUID()
		val exception = assertThrows(NoSuchElementException::class.java) {
			controller.hentGjennomforing(id)
		}
		assertEquals("Fant ikke gjennomforing med id $id", exception.message)
	}

	@Test
	fun `hentGjennomforinger - tiltak finnes - skal returnere gjennomføring med tiltak`() {
		val gjennomforingCmd = createGjennomforingInput(TILTAK_1, ARRANGOR_1, NAV_ENHET_1)
		testDataRepository.insertGjennomforing(gjennomforingCmd)

		val resultat = controller.hentGjennomforing(gjennomforingCmd.id)

		resultat.id shouldBe gjennomforingCmd.id
		resultat.navn shouldBe gjennomforingCmd.navn
		resultat.tiltak.tiltaksnavn shouldBe TILTAK_1.navn
		resultat.arrangor.organisasjonNavn shouldBe ARRANGOR_1.overordnetEnhetNavn

	}

	@Test
	fun `hentDeltakere - En deltaker på tiltak - returnerer deltaker`() {
		val bruker = BRUKER_1
		val gjennomforingCmd = createGjennomforingInput(TILTAK_1, ARRANGOR_1, NAV_ENHET_1)
		val deltakerCmd = createDeltakerInput(bruker, gjennomforingCmd)
		testDataRepository.insertGjennomforing(gjennomforingCmd)
		testDataRepository.insertDeltaker(deltakerCmd)
		testDataRepository.insertDeltakerStatus(createStatusInput(deltakerCmd))

		val deltakere = controller.hentDeltakere(gjennomforingCmd.id)
		deltakere.size shouldBe 1

		val deltaker1 = deltakere[0]

		deltaker1.fodselsnummer shouldBe bruker.fodselsnummer
		deltaker1.fornavn shouldBe bruker.fornavn
		deltaker1.etternavn shouldBe bruker.etternavn

		deltaker1.startDato shouldBe deltakerCmd.startDato
		deltaker1.sluttDato shouldBe deltakerCmd.sluttDato
		deltaker1.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe deltaker1.registrertDato.truncatedTo(ChronoUnit.MINUTES)

	}

	@Test
	fun `hentDeltakere - Flere deltakere finnes - henter alle`() {
		val gjennomforingCmd = createGjennomforingInput(TILTAK_1, ARRANGOR_1, NAV_ENHET_1)
		val deltakerCmd = createDeltakerInput(BRUKER_1, gjennomforingCmd)
		val bruker2Cmd = createBrukerInput(NAV_ENHET_1)
		val deltaker2Cmd = createDeltakerInput(bruker2Cmd, gjennomforingCmd)

		testDataRepository.insertGjennomforing(gjennomforingCmd)

		testDataRepository.insertDeltaker(deltakerCmd)
		testDataRepository.insertDeltakerStatus(createStatusInput(deltakerCmd))
		testDataRepository.insertBruker(bruker2Cmd)
		testDataRepository.insertDeltaker(deltaker2Cmd)
		testDataRepository.insertDeltakerStatus(createStatusInput(deltaker2Cmd))
		val deltakere = controller.hentDeltakere(gjennomforingCmd.id)

		deltakere.size shouldBe 2

	}

	@Test
	fun `hentDeltakere - Har endringsmeldinger - finner aktiv endringsmenlding`() {
		val endringsmeldingDato = LocalDate.now().plusDays(7)
		val gjennomforingCmd = createGjennomforingInput(TILTAK_1, ARRANGOR_1, NAV_ENHET_1)
		val deltakerCmd = createDeltakerInput(BRUKER_1, gjennomforingCmd)
		val aktivEndringsmeldingCmd = EndringsmeldingInput(
			id = UUID.randomUUID(),
			deltakerId = deltakerCmd.id,
			startDato = endringsmeldingDato,
			aktiv = true,
			opprettetAvArrangorAnsattId = TestData.ARRANGOR_ANSATT_1.id,
		)
		val inaktivEndringsmeldingCmd = EndringsmeldingInput(
			id = UUID.randomUUID(),
			deltakerId = deltakerCmd.id,
			startDato = endringsmeldingDato.minusDays(8),
			aktiv = false,
			opprettetAvArrangorAnsattId = TestData.ARRANGOR_ANSATT_1.id,
		)

		testDataRepository.insertGjennomforing(gjennomforingCmd)

		testDataRepository.insertDeltaker(deltakerCmd)
		testDataRepository.insertDeltakerStatus(createStatusInput(deltakerCmd))
		testDataRepository.insertEndringsmelding(inaktivEndringsmeldingCmd)
		testDataRepository.insertEndringsmelding(aktivEndringsmeldingCmd)
		val deltakere = controller.hentDeltakere(gjennomforingCmd.id)

		deltakere.find { it.id == deltakerCmd.id }!!.aktivEndringsmelding!!.startDato shouldBe endringsmeldingDato
	}

	@Test
	fun `hentDeltakere - Utdatert status på deltaker - filtreres bort `() {
		val gjennomforingCmd = createGjennomforingInput(TILTAK_1, ARRANGOR_1, NAV_ENHET_1)
		val deltakerCmd = createDeltakerInput(BRUKER_1, gjennomforingCmd)
		val bruker2Cmd = createBrukerInput(NAV_ENHET_1)
		val deltaker2Cmd = createDeltakerInput(bruker2Cmd, gjennomforingCmd)
		val gammelStatus = createStatusInput(deltaker2Cmd)
			.copy(status = Deltaker.Status.HAR_SLUTTET.name, gyldigFra = LocalDateTime.now().minusDays(15))
		testDataRepository.insertGjennomforing(gjennomforingCmd)

		testDataRepository.insertDeltaker(deltakerCmd)
		testDataRepository.insertDeltakerStatus(createStatusInput(deltakerCmd))
		testDataRepository.insertBruker(bruker2Cmd)
		testDataRepository.insertDeltaker(deltaker2Cmd)
		testDataRepository.insertDeltakerStatus(gammelStatus)

		val deltakere = controller.hentDeltakere(gjennomforingCmd.id)

		deltakere.size shouldBe 1
		deltakere.first().id shouldBe deltakerCmd.id

	}

}
