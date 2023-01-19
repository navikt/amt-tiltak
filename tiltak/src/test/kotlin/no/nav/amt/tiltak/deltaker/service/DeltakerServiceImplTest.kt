package no.nav.amt.tiltak.deltaker.service

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.SkjermetPersonService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.SkjultDeltakerRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_2
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
	lateinit var skjultDeltakerRepository: SkjultDeltakerRepository
	lateinit var skjermetPersonService: SkjermetPersonService

	val dataSource = SingletonPostgresContainer.getDataSource()
	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	val deltakerId = UUID.randomUUID()

	@BeforeEach
	fun beforeEach() {
		brukerRepository = BrukerRepository(jdbcTemplate)

		navEnhetService = mockk()
		endringsmeldingService = mockk()
		skjermetPersonService = mockk()
		brukerService = BrukerService(brukerRepository, mockk(), mockk(), navEnhetService, skjermetPersonService)
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		skjultDeltakerRepository = SkjultDeltakerRepository(jdbcTemplate)

		deltakerServiceImpl = DeltakerServiceImpl(
			deltakerRepository = deltakerRepository,
			deltakerStatusRepository = deltakerStatusRepository,
			brukerService = brukerService,
			endringsmeldingService = endringsmeldingService,
			skjultDeltakerRepository = skjultDeltakerRepository,
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
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.oppdaterStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET

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
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.oppdaterStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET

	}

	@Test
	fun `oppdaterStatuser - Setter til ikke aktuell, aktive deltakere deltakere på avsluttede gjennomføringer`() {
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertNavEnhet(NAV_ENHET_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_2)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_2.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_2)

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.VENTER_PA_OPPSTART

		deltakerServiceImpl.oppdaterStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.IKKE_AKTUELL

	}
	@Test
	fun `upsertDeltaker - inserter ny deltaker`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)

		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent,deltaker.gjennomforingId)

		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId
	}

	@Test
	fun `insertStatus - ingester status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, GJENNOMFORING_1.id)
		val now = LocalDate.now().atStartOfDay()

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = DeltakerStatus.Type.IKKE_AKTUELL,
			aarsak = null,
			gyldigFra = now
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		val statusEtterEndring = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		statusEtterEndring shouldNotBe null
		statusEtterEndring!!.copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			id = statusInsertDbo.id,
			deltakerId = nyDeltaker.id,
			type = statusInsertDbo.type,
			gyldigFra = statusInsertDbo.gyldigFra!!,
			aarsak = statusInsertDbo.aarsak,
			opprettetDato = now,
			aktiv = true
		)

	}

	@Test
	fun `insertStatus - deltaker får samme status igjen - oppdaterer ikke status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, GJENNOMFORING_1.id)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = DeltakerStatus.Type.IKKE_AKTUELL,
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
			type = DeltakerStatus.Type.IKKE_AKTUELL,
			aarsak = null,
			gyldigFra = LocalDateTime.now()
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo)

		deltakerServiceImpl.insertStatus(statusInsertDbo.copy(id = UUID.randomUUID(), type = DeltakerStatus.Type.VENTER_PA_OPPSTART))

		val statuser = deltakerStatusRepository.getStatuserForDeltaker(nyDeltaker.id)

		statuser.size shouldBe 2
		statuser.first().aktiv shouldBe false
		statuser.first().type shouldBe statusInsertDbo.type
		statuser.last().aktiv shouldBe true
	}

	@Test
	fun `slettDeltaker - skal slette deltaker og status`() {
		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(2)
		)

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)
		deltakerServiceImpl.insertStatus(statusInsertDbo)

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerServiceImpl.slettDeltaker(deltakerId)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null
	}

	@Test
	fun `hentDeltakerePaaGjennomforing - skal hente alle deltakere med riktig status pa gjennomforing`() {
		val deltakerUpserts = listOf(
			deltaker.copy(id = UUID.randomUUID()),
			deltaker.copy(id = UUID.randomUUID()),
			deltaker.copy(id = UUID.randomUUID()),
		)

		val statusInserts = listOf(
			statusInsert.copy(id = UUID.randomUUID(), type = DeltakerStatus.Type.VENTER_PA_OPPSTART, deltakerId = deltakerUpserts[0].id),
			statusInsert.copy(id = UUID.randomUUID(), type = DeltakerStatus.Type.DELTAR, deltakerId = deltakerUpserts[1].id),
			statusInsert.copy(id = UUID.randomUUID(), type = DeltakerStatus.Type.HAR_SLUTTET, deltakerId = deltakerUpserts[2].id),
		)

		deltakerUpserts.forEach {
			deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, it)
		}
		statusInserts.forEach {
			deltakerServiceImpl.insertStatus(it)
		}

		val deltakere = deltakerServiceImpl.hentDeltakerePaaGjennomforing(GJENNOMFORING_1.id)
		deltakere.find { it.id == deltakerUpserts[0].id }!!.status.type shouldBe statusInserts[0].type
		deltakere.find { it.id == deltakerUpserts[1].id }!!.status.type shouldBe statusInserts[1].type
		deltakere.find { it.id == deltakerUpserts[2].id }!!.status.type shouldBe statusInserts[2].type
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor - skal skjule deltaker`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "IKKE_AKTUELL"))

		deltakerServiceImpl.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)

		deltakerServiceImpl.erSkjultForTiltaksarrangor(deltakerId) shouldBe true
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor - skal kaste exception hvis deltaker har ugyldig status`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1.copy(id = UUID.randomUUID(), deltakerId = deltakerId, status = "DELTAR"))

		shouldThrowExactly<IllegalStateException> {
			deltakerServiceImpl.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)
		}
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

	val statusInsert = DeltakerStatusInsert(
		id = UUID.randomUUID(),
		deltakerId = UUID.randomUUID(),
		type = DeltakerStatus.Type.DELTAR,
		aarsak = null,
		gyldigFra = LocalDateTime.now().minusDays(7),
	)

}
