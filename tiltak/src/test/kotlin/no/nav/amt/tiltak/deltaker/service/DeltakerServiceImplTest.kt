package no.nav.amt.tiltak.deltaker.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.SkjultDeltakerRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
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
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_KURS
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
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
	lateinit var endringsmeldingService: EndringsmeldingServiceImpl
	lateinit var endringsmeldingRepository: EndringsmeldingRepository
	lateinit var skjultDeltakerRepository: SkjultDeltakerRepository
	lateinit var gjennomforingService: GjennomforingService
	lateinit var kafkaProducerService: KafkaProducerService
	lateinit var objectMapper: ObjectMapper
	lateinit var publisherService: DataPublisherService
	lateinit var amtPersonClient: AmtPersonClient

	val dataSource = SingletonPostgresContainer.getDataSource()
	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	val deltakerId = UUID.randomUUID()
	val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

	@BeforeEach
	fun beforeEach() {
		brukerRepository = BrukerRepository(jdbcTemplate)

		navEnhetService = mockk()
		endringsmeldingService = mockk()
		kafkaProducerService = mockk(relaxUnitFun = true)
		publisherService = mockk()
		amtPersonClient = mockk(relaxUnitFun = true)
		brukerService = BrukerServiceImpl(brukerRepository, mockk(), mockk(), navEnhetService, amtPersonClient)
		objectMapper = JsonUtils.objectMapper
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		skjultDeltakerRepository = SkjultDeltakerRepository(jdbcTemplate)
		gjennomforingService = mockk()
		endringsmeldingRepository = EndringsmeldingRepository(jdbcTemplate, objectMapper)
		endringsmeldingService = EndringsmeldingServiceImpl(endringsmeldingRepository, mockk(), transactionTemplate, publisherService)

		deltakerServiceImpl = DeltakerServiceImpl(
			deltakerRepository = deltakerRepository,
			deltakerStatusRepository = deltakerStatusRepository,
			brukerService = brukerService,
			endringsmeldingService = endringsmeldingService,
			skjultDeltakerRepository = skjultDeltakerRepository,
			gjennomforingService = gjennomforingService,
			transactionTemplate = transactionTemplate,
			kafkaProducerService = kafkaProducerService,
			publisherService = publisherService
		)
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
		every { publisherService.publish(any(), any()) } returns Unit
	}

	@AfterEach
	fun afterEach() {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	@Test
	fun `oppdaterStatuser - Sluttdato har passert, ikke kurs - Setter status til har slutta`() {
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertDeltaker(DELTAKER_2)
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET

	}

	@Test
	fun `oppdaterStatuser - Sluttdato har passert, kurs - Setter status til har slutta`() {
		val gjennomforingInput = GJENNOMFORING_KURS.copy(sluttDato = LocalDate.now().minusDays(1))
		testDataRepository.insertGjennomforing(gjennomforingInput)
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertDeltaker(DELTAKER_2.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(gjennomforingInput.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET

	}

	@Test
	fun `oppdaterStatuser - Sluttdato har passert og er før kursets sluttdato, kurs - Setter status til avbrutt`() {
		val gjennomforingInput = GJENNOMFORING_KURS.copy(sluttDato = LocalDate.now().minusDays(1))
		testDataRepository.insertGjennomforing(gjennomforingInput)
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertDeltaker(DELTAKER_2.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato.minusDays(1)))
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(GJENNOMFORING_KURS.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status!!.type shouldBe DeltakerStatus.Type.AVBRUTT

	}

	@Test
	fun `oppdaterStatuser - Avslutter aktive deltakere deltakere på avsluttede gjennomføringer`() {
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertNavEnhet(NAV_ENHET_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_2)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_2.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET

	}

	@Test
	fun `oppdaterStatuser - aktive deltakere deltakere på avsluttede gjennomføringer - Setter til ikke aktuell`() {
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertNavEnhet(NAV_ENHET_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_2)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_2.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_2)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_2.toArrangor()))

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.VENTER_PA_OPPSTART

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.IKKE_AKTUELL

	}

	@Test
	fun `upsertDeltaker - inserter ny deltaker`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)

		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, deltaker.gjennomforingId)

		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId

		verify(exactly = 1) { kafkaProducerService.publiserDeltaker(any()) }
	}

	@Test
	fun `insertStatus - skal publisere endring på kafka`() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)

		deltakerServiceImpl.insertStatus(
			DeltakerStatusInsert(
				id = UUID.randomUUID(),
				deltakerId = DELTAKER_1.id,
				type = DeltakerStatus.Type.HAR_SLUTTET,
				aarsak = null,
				gyldigFra = LocalDateTime.now().minusHours(1)
			)
		)

		verify(exactly = 1) { kafkaProducerService.publiserDeltaker(any()) }
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

		deltakerServiceImpl.insertStatus(
			statusInsertDbo.copy(
				id = UUID.randomUUID(),
				type = DeltakerStatus.Type.VENTER_PA_OPPSTART
			)
		)

		val statuser = deltakerStatusRepository.getStatuserForDeltaker(nyDeltaker.id)

		statuser.size shouldBe 2
		statuser.first().aktiv shouldBe false
		statuser.first().type shouldBe statusInsertDbo.type
		statuser.last().aktiv shouldBe true
	}

	@Test
	fun `slettDeltaker - skal publisere sletting på kafka`() {
		deltakerServiceImpl.slettDeltaker(deltakerId)

		verify(exactly = 1) { kafkaProducerService.publiserSlettDeltaker(deltakerId) }
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
	fun `slettDeltaker - skal slette deltaker, status og endringsmeldinger`() {
		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(2)
		)

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker)
		deltakerServiceImpl.insertStatus(statusInsertDbo)
		endringsmeldingService.opprettForlengDeltakelseEndringsmelding(
			deltaker.id,
			ARRANGOR_ANSATT_1.id,
			LocalDate.now().plusWeeks(4),
		)

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerServiceImpl.slettDeltaker(deltakerId)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null

		endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(deltakerId) shouldHaveSize 0
	}

	@Test
	fun `hentDeltakerePaaGjennomforing - skal hente alle deltakere med riktig status pa gjennomforing`() {
		val deltaker1 = DELTAKER_1.copy(id = UUID.randomUUID())
		val deltaker2 = DELTAKER_1.copy(id = UUID.randomUUID())
		val deltaker3 = DELTAKER_1.copy(id = UUID.randomUUID())

		val deltakerStatus1 = DELTAKER_1_STATUS_1.copy(
			id = UUID.randomUUID(),
			deltakerId = deltaker1.id,
			status = DeltakerStatus.Type.VENTER_PA_OPPSTART.name
		)
		val deltakerStatus2 = DELTAKER_1_STATUS_1.copy(
			id = UUID.randomUUID(),
			deltakerId = deltaker2.id,
			status = DeltakerStatus.Type.DELTAR.name
		)
		val deltakerStatus3 = DELTAKER_1_STATUS_1.copy(
			id = UUID.randomUUID(),
			deltakerId = deltaker3.id,
			status = DeltakerStatus.Type.HAR_SLUTTET.name
		)

		testDataRepository.insertDeltaker(deltaker1)
		testDataRepository.insertDeltaker(deltaker2)
		testDataRepository.insertDeltaker(deltaker3)

		testDataRepository.insertDeltakerStatus(deltakerStatus1)
		testDataRepository.insertDeltakerStatus(deltakerStatus2)
		testDataRepository.insertDeltakerStatus(deltakerStatus3)

		val deltakere = deltakerServiceImpl.hentDeltakerePaaGjennomforing(GJENNOMFORING_1.id)
		deltakere.find { it.id == deltaker1.id }!!.status.type.name shouldBe deltakerStatus1.status
		deltakere.find { it.id == deltaker2.id }!!.status.type.name shouldBe deltakerStatus2.status
		deltakere.find { it.id == deltaker3.id }!!.status.type.name shouldBe deltakerStatus3.status
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor - skal skjule deltaker`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(
			DELTAKER_1_STATUS_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				status = "IKKE_AKTUELL"
			)
		)

		deltakerServiceImpl.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)

		deltakerServiceImpl.erSkjultForTiltaksarrangor(deltakerId) shouldBe true
	}

	@Test
	fun `skjulDeltakerForTiltaksarrangor - skal kaste exception hvis deltaker har ugyldig status`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(
			DELTAKER_1_STATUS_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				status = "DELTAR"
			)
		)

		shouldThrowExactly<IllegalStateException> {
			deltakerServiceImpl.skjulDeltakerForTiltaksarrangor(deltakerId, ARRANGOR_ANSATT_1.id)
		}
	}

	@Test
	fun `republiserAlleDeltakerePaKafka - skal publisere deltakere på kafka`() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)

		deltakerServiceImpl.republiserAlleDeltakerePaKafka(1)

		verify(exactly = 2) { kafkaProducerService.publiserDeltaker(any()) }
	}

	val statusInsert = DeltakerStatusInsert(
		id = UUID.randomUUID(),
		deltakerId = deltakerId,
		type = DeltakerStatus.Type.DELTAR,
		aarsak = null,
		gyldigFra = LocalDateTime.now().minusDays(7),
	)

	val deltaker = DeltakerUpsert(
		id = deltakerId,
		statusInsert = statusInsert,
		startDato = null,
		sluttDato = null,
		registrertDato = LocalDateTime.now(),
		dagerPerUke = null,
		prosentStilling = null,
		gjennomforingId = GJENNOMFORING_1.id,
		innsokBegrunnelse = null
	)

}
