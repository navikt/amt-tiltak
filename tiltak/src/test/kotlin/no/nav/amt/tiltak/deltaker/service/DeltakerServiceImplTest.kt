package no.nav.amt.tiltak.deltaker.service

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import no.nav.amt.tiltak.tiltak.services.BrukerService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerServiceImplTest {
	lateinit var deltakerRepository: DeltakerRepository
	lateinit var deltakerStatusRepository: DeltakerStatusRepository
	lateinit var deltakerServiceImpl: DeltakerServiceImpl
	lateinit var brukerRepository: BrukerRepository
	lateinit var brukerService: BrukerService
	lateinit var testDataRepository: TestDataRepository
	lateinit var navEnhetService: NavEnhetService
	lateinit var endringsmeldingService: EndringsmeldingService

	val dataSource = SingletonPostgresContainer.getDataSource()
	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	val deltakerId = UUID.randomUUID()

	@BeforeEach
	fun beforeEach() {
		brukerRepository = BrukerRepository(jdbcTemplate)

		navEnhetService = mockk()
		endringsmeldingService = mockk()
		brukerService = BrukerService(brukerRepository, mockk(), mockk(), navEnhetService)
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		deltakerServiceImpl = DeltakerServiceImpl(
			deltakerRepository = deltakerRepository,
			deltakerStatusRepository = deltakerStatusRepository,
			brukerService = brukerService,
			endringsmeldingService = endringsmeldingService,
			transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource)),
		)
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
	}

	@AfterEach
	fun afterEach() {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	@Test
	fun `oppdaterStatuser - Avslutter aktive deltakere med passert tildato`() {
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertDeltaker(DELTAKER_2)
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.status shouldBe Deltaker.Status.DELTAR

		deltakerServiceImpl.oppdaterStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)

		status!!.status shouldBe Deltaker.Status.HAR_SLUTTET

	}

	@Test
	fun `oppdaterStatuser - Avslutter aktive deltakere deltakere på avsluttede gjennomføringer`() {
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertNavEnhet(NAV_ENHET_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_2)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_2.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.status shouldBe Deltaker.Status.DELTAR

		deltakerServiceImpl.oppdaterStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.status shouldBe Deltaker.Status.HAR_SLUTTET

	}
	@Test
	fun `upsertDeltaker - inserter ny deltaker`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.fodselsnummer, deltaker)

		val nyDeltaker = deltakerRepository.get(BRUKER_1.fodselsnummer,deltaker.gjennomforingId)

		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId
	}

	@Test
	fun `insertStatus - ingester status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.fodselsnummer, deltaker)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.fodselsnummer, GJENNOMFORING_1.id)
		val now = LocalDate.now().atStartOfDay()

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			aarsak = null,
			gyldigFra = now
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		val statusEtterEndring = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		statusEtterEndring shouldNotBe null
		statusEtterEndring!!.copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			id = statusInsertDbo.id,
			deltakerId = nyDeltaker.id,
			status = statusInsertDbo.type,
			gyldigFra = statusInsertDbo.gyldigFra!!,
			aarsak = statusInsertDbo.aarsak,
			opprettetDato = now,
			aktiv = true
		)

	}

	@Test
	fun `insertStatus - deltaker får samme status igjen - oppdaterer ikke status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.fodselsnummer, deltaker)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.fodselsnummer, GJENNOMFORING_1.id)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			aarsak = null,
			gyldigFra = LocalDateTime.now()
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		val status1 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		val status2 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		status2 shouldBe status1

	}

	@Test
	fun `insertStatus - deltaker ny status - setter ny og deaktiverer den gamle`() {
		val deltakerCmd = createDeltakerInput(BRUKER_1, GJENNOMFORING_1)
		testDataRepository.insertDeltaker(deltakerCmd)

		val nyDeltaker = deltakerRepository.get(deltakerCmd.brukerId, deltakerCmd.gjennomforingId)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			aarsak = null,
			gyldigFra = LocalDateTime.now()
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		deltakerServiceImpl.insertStatus(statusInsertDbo.copy(id = UUID.randomUUID(), type = Deltaker.Status.VENTER_PA_OPPSTART))

		val statuser = deltakerStatusRepository.getStatuserForDeltaker(nyDeltaker.id)

		statuser.size shouldBe 2
		statuser.first().aktiv shouldBe false
		statuser.first().status shouldBe statusInsertDbo.type
		statuser.last().aktiv shouldBe true
	}

	@Test
	fun `slettDeltaker - skal slette deltaker og status`() {
		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			type = Deltaker.Status.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(2)
		)

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.fodselsnummer, deltaker)
		deltakerServiceImpl.insertStatus(statusInsertDbo)

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerServiceImpl.slettDeltaker(deltakerId)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null
	}

	val deltaker = DeltakerUpsert(
		id =  deltakerId,
		startDato = null,
		sluttDato = null,
		registrertDato =  LocalDateTime.now(),
		dagerPerUke = null,
		prosentStilling = null,
		gjennomforingId = GJENNOMFORING_1.id,
		innsokBegrunnelse = null
	)
}
