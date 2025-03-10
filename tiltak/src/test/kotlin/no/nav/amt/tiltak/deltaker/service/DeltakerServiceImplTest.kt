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
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.domain.tiltak.Vurderingstype
import no.nav.amt.tiltak.core.exceptions.ValidationException
import no.nav.amt.tiltak.core.kafka.DeltakerV1ProducerService
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_4
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_4
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_4_STATUS_1
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
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo

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
	lateinit var gjennomforingService: GjennomforingService
	lateinit var deltakerV1ProducerService: DeltakerV1ProducerService
	lateinit var objectMapper: ObjectMapper
	lateinit var publisherService: DataPublisherService
	lateinit var amtPersonClient: AmtPersonClient
	lateinit var navAnsattService: NavAnsattService
	lateinit var vurderingRepository: VurderingRepository
	lateinit var unleashService: UnleashService

	val dataSource = SingletonPostgresContainer.getDataSource()
	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	val deltakerId = UUID.randomUUID()
	val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

	@BeforeEach
	fun beforeEach() {
		brukerRepository = BrukerRepository(jdbcTemplate)

		navEnhetService = mockk()
		endringsmeldingService = mockk()
		deltakerV1ProducerService = mockk(relaxUnitFun = true)
		publisherService = mockk()
		amtPersonClient = mockk(relaxUnitFun = true)
		navAnsattService = mockk(relaxUnitFun = true)
		brukerService = BrukerServiceImpl(brukerRepository, navEnhetService, navAnsattService, amtPersonClient)
		objectMapper = JsonUtils.objectMapper
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		gjennomforingService = mockk()
		endringsmeldingRepository = EndringsmeldingRepository(jdbcTemplate, objectMapper)
		endringsmeldingService = EndringsmeldingServiceImpl(endringsmeldingRepository, mockk(), transactionTemplate, publisherService)
		vurderingRepository = VurderingRepository(jdbcTemplate)
		unleashService = mockk()

		deltakerServiceImpl = DeltakerServiceImpl(
			deltakerRepository = deltakerRepository,
			deltakerStatusRepository = deltakerStatusRepository,
			brukerService = brukerService,
			endringsmeldingService = endringsmeldingService,
			gjennomforingService = gjennomforingService,
			transactionTemplate = transactionTemplate,
			deltakerV1Producer = deltakerV1ProducerService,
			publisherService = publisherService,
			vurderingRepository = vurderingRepository,
			unleashService = unleashService
		)
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
		every { publisherService.publish(id = any(), type = any(), erKometDeltaker = any(), forcePublish = any()) } returns Unit
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
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
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET
		verify(exactly = 1) { publisherService.publish(DELTAKER_2.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `oppdaterStatuser - Sluttdato har passert, kurs - Setter status til fullfort`() {
		val gjennomforingInput = GJENNOMFORING_KURS.copy(sluttDato = LocalDate.now().minusDays(1))
		testDataRepository.insertGjennomforing(gjennomforingInput)
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertDeltaker(DELTAKER_2.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(gjennomforingInput.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status!!.type shouldBe DeltakerStatus.Type.FULLFORT
		verify(exactly = 1) { publisherService.publish(DELTAKER_2.id, DataPublishType.DELTAKER, false) }
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
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_1.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status!!.type shouldBe DeltakerStatus.Type.AVBRUTT
		verify(exactly = 1) { publisherService.publish(DELTAKER_2.id, DataPublishType.DELTAKER, false) }
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
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET
		verify(exactly = 1) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }
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
		every {
			gjennomforingService.getGjennomforing(GJENNOMFORING_2.id)
		} returns GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_2.toArrangor())

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.VENTER_PA_OPPSTART

		deltakerServiceImpl.progressStatuser()

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.IKKE_AKTUELL
		verify(exactly = 1) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `avsluttDeltakerePaaAvbruttGjennomforing - Avslutter aktive deltakere deltakere pa avbrutt gjennomforing med sluttarsak`() {
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertNavEnhet(NAV_ENHET_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_2)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_2.id))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		every {
			gjennomforingService.getGjennomforinger(any())
		} returns listOf(GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor()))
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_2.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		// Valider testdata tilstand
		val forrigeStatus = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		GJENNOMFORING_2.status shouldBe Gjennomforing.Status.AVSLUTTET.name
		forrigeStatus!!.type shouldBe DeltakerStatus.Type.DELTAR

		deltakerServiceImpl.avsluttDeltakerePaaAvbruttGjennomforing(GJENNOMFORING_2.id)

		val status = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)

		status!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET
		status.aarsak shouldBe DeltakerStatus.Aarsak.SAMARBEIDET_MED_ARRANGOREN_ER_AVBRUTT
		verify(exactly = 1) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `upsertDeltaker - inserter ny deltaker`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)

		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, deltaker.gjennomforingId)

		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId

		verify(exactly = 1) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 1) { publisherService.publish(deltaker.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `upsertDeltaker - republiserer ikke uendrede deltakere`() {
		verify(exactly = 0) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)

		verify(exactly = 1) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 1) { publisherService.publish(deltaker.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `upsertDeltaker - oppdaterer forrige deltaker`() {
		val dagerPerUke = 2.5f
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker.copy(dagerPerUke = dagerPerUke), false)

		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, deltaker.gjennomforingId)

		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId
		nyDeltaker.dagerPerUke shouldBe dagerPerUke

		verify(exactly = 2) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 2) { publisherService.publish(deltaker.id, DataPublishType.DELTAKER, false) }
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
				aarsaksbeskrivelse = null,
				gyldigFra = LocalDateTime.now().minusHours(1)
			),
			false
		)

		verify(exactly = 1) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 1) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `insertStatus - ingester status`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, GJENNOMFORING_1.id)
		val now = LocalDate.now().atStartOfDay()

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = DeltakerStatus.Type.IKKE_AKTUELL,
			aarsak = null,
			aarsaksbeskrivelse = null,
			gyldigFra = now
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo, false)

		val statusEtterEndring = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		statusEtterEndring shouldNotBe null
		statusEtterEndring!!.copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			id = statusInsertDbo.id,
			deltakerId = nyDeltaker.id,
			type = statusInsertDbo.type,
			gyldigFra = statusInsertDbo.gyldigFra!!,
			aarsak = statusInsertDbo.aarsak,
			aarsaksbeskrivelse = statusInsertDbo.aarsaksbeskrivelse,
			opprettetDato = now,
			aktiv = true
		)
		verify(exactly = 2) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 2) { publisherService.publish(nyDeltaker.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `insertStatus - deltaker får samme status igjen - oppdaterer ikke status`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		val nyDeltaker = deltakerRepository.get(BRUKER_1.personIdent, GJENNOMFORING_1.id)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = DeltakerStatus.Type.IKKE_AKTUELL,
			aarsak = null,
			aarsaksbeskrivelse = null,
			gyldigFra = LocalDateTime.now()
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo, false)

		val status1 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		deltakerServiceImpl.insertStatus(statusInsertDbo, false)

		val status2 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		status2 shouldBe status1
		verify(exactly = 2) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 2) { publisherService.publish(nyDeltaker.id, DataPublishType.DELTAKER, false) }
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
			aarsaksbeskrivelse = null,
			gyldigFra = LocalDateTime.now()
		)

		deltakerServiceImpl.insertStatus(statusInsertDbo, false)

		deltakerServiceImpl.insertStatus(
			statusInsertDbo.copy(
				id = UUID.randomUUID(),
				type = DeltakerStatus.Type.VENTER_PA_OPPSTART
			),
			false
		)

		val statuser = deltakerStatusRepository.getStatuserForDeltaker(nyDeltaker.id)

		statuser.size shouldBe 2
		statuser.first().aktiv shouldBe false
		statuser.first().type shouldBe statusInsertDbo.type
		statuser.last().aktiv shouldBe true
		verify(exactly = 2) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 2) { publisherService.publish(nyDeltaker.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `slettDeltaker - skal publisere sletting på kafka`() {
		deltakerServiceImpl.slettDeltaker(deltakerId, false)

		verify(exactly = 1) { deltakerV1ProducerService.publiserSlettDeltaker(deltakerId) }
		verify(exactly = 1) { publisherService.publish(deltakerId, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `slettDeltaker - skal slette deltaker og status`() {
		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			aarsaksbeskrivelse = null,
			gyldigFra = LocalDateTime.now().minusDays(2)
		)
		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		deltakerServiceImpl.insertStatus(statusInsertDbo, false)
		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerServiceImpl.slettDeltaker(deltakerId, false)

		deltakerRepository.get(deltakerId) shouldBe null
		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null
	}

	@Test
	fun `slettDeltaker - skal slette deltaker, status, endringsmeldinger, og vurdering`() {
		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = deltaker.id,
			type = DeltakerStatus.Type.VURDERES,
			aarsak = null,
			aarsaksbeskrivelse = null,
			gyldigFra = LocalDateTime.now().minusDays(2)
		)

		deltakerServiceImpl.upsertDeltaker(BRUKER_1.personIdent, deltaker, false)
		deltakerServiceImpl.insertStatus(statusInsertDbo, false)
		endringsmeldingService.opprettForlengDeltakelseEndringsmelding(
			deltaker.id,
			ARRANGOR_ANSATT_1.id,
			LocalDate.now().plusWeeks(4),
		)
		deltakerServiceImpl.lagreVurdering(
			Vurdering(
				id = UUID.randomUUID(),
				deltakerId = deltaker.id,
				opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
				vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
				begrunnelse = null,
				opprettet = LocalDateTime.now(),
			)
		)

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerServiceImpl.slettDeltaker(deltakerId, false)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null

		endringsmeldingService.hentAktiveEndringsmeldingerForDeltaker(deltakerId) shouldHaveSize 0

		vurderingRepository.getVurderingerForDeltaker(deltaker.id) shouldBe emptyList()
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
	fun `republiserAlleDeltakerePaKafka - skal publisere deltakere på kafka`() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_KURS.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		deltakerServiceImpl.republiserAlleDeltakerePaKafka(1)

		verify(exactly = 2) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 2) { publisherService.publish(any(), DataPublishType.DELTAKER, false, true) }
	}

	@Test
	fun `lagreVurdering - deltaker har status IKKE_AKTUELL - kaster IllegalStateException`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(
			DELTAKER_1_STATUS_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				status = "IKKE_AKTUELL"
			)
		)

		shouldThrowExactly<ValidationException> {
			deltakerServiceImpl.lagreVurdering(
				Vurdering(
					id = UUID.randomUUID(),
					deltakerId = deltakerId,
					opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
					vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
					begrunnelse = null,
					opprettet = LocalDateTime.now(),
				)
			)
		}
	}

	@Test
	fun `lagreVurdering - deltaker har status VURDERES og ingen vurderinger fra for - lagrer vurdering`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(
			DELTAKER_1_STATUS_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				status = "VURDERES"
			)
		)

		val vurderinger = deltakerServiceImpl.lagreVurdering(
			Vurdering(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
				vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
				begrunnelse = null,
				opprettet = LocalDateTime.now(),
			)
		)

		vurderinger.size shouldBe 1
		val lagretVurdering = vurderinger.first()
		lagretVurdering.deltakerId shouldBe deltakerId
		lagretVurdering.vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
		lagretVurdering.begrunnelse shouldBe null
		lagretVurdering.opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
		lagretVurdering.opprettet shouldBeCloseTo LocalDateTime.now()
		verify(exactly = 0) { deltakerV1ProducerService.publiserDeltaker(any(), any()) }
		verify(exactly = 1) { publisherService.publish(deltakerId, DataPublishType.DELTAKER, null) }
	}

	@Test
	fun `lagreVurdering - deltaker har status VURDERES og en vurdering fra for - lagrer vurdering og setter gyldigTil for forrige vurdering`() {
		val deltakerId = UUID.randomUUID()

		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = deltakerId))
		testDataRepository.insertDeltakerStatus(
			DELTAKER_1_STATUS_1.copy(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				status = "VURDERES"
			)
		)
		val forrigeVurdering = VurderingDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerId,
			begrunnelse = "Mangler førerkort",
			vurderingstype = Vurderingstype.OPPFYLLER_IKKE_KRAVENE,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
			gyldigFra = LocalDateTime.now().minusDays(2),
			gyldigTil = null
		)
		vurderingRepository.insert(forrigeVurdering)

		val vurderinger = deltakerServiceImpl.lagreVurdering(
			Vurdering(
				id = UUID.randomUUID(),
				deltakerId = deltakerId,
				opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
				vurderingstype = Vurderingstype.OPPFYLLER_KRAVENE,
				begrunnelse = null,
				opprettet = LocalDateTime.now(),
			)
		)

		vurderinger.size shouldBe 2
		val forrigeVurderingFraDb = vurderinger.find { it.id == forrigeVurdering.id }
		forrigeVurderingFraDb?.vurderingstype shouldBe Vurderingstype.OPPFYLLER_IKKE_KRAVENE
		forrigeVurderingFraDb?.begrunnelse shouldBe forrigeVurdering.begrunnelse
		forrigeVurderingFraDb!!.opprettet shouldBeEqualTo forrigeVurdering.gyldigFra

		val nyVurderingFraDb = vurderinger.find { it.id != forrigeVurdering.id }
		nyVurderingFraDb?.vurderingstype shouldBe Vurderingstype.OPPFYLLER_KRAVENE
		nyVurderingFraDb?.begrunnelse shouldBe null
		nyVurderingFraDb?.opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
		nyVurderingFraDb?.opprettet!! shouldBeCloseTo LocalDateTime.now()
	}

	@Test
	fun `konverterStatuserForDeltakerePaaGjennomforing - fra kurs til lopende - setter riktig status pa deltakere`() {
		val gjennomforingInput = GJENNOMFORING_KURS.copy(sluttDato = LocalDate.now().plusYears(1))
		testDataRepository.insertGjennomforing(gjennomforingInput)
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertBruker(BRUKER_4)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltaker(DELTAKER_2.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato.minusDays(4)))
		testDataRepository.insertDeltaker(DELTAKER_4.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_2)
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1.copy(status = DeltakerStatus.Type.AVBRUTT.name, aarsak = DeltakerStatus.Aarsak.FATT_JOBB.name))
		testDataRepository.insertDeltakerStatus(DELTAKER_4_STATUS_1.copy(status = DeltakerStatus.Type.FULLFORT.name, aarsak = null))
		every {
			gjennomforingService.getGjennomforing(any())
		} returns GJENNOMFORING_KURS.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		deltakerServiceImpl.konverterStatuserForDeltakerePaaGjennomforing(GJENNOMFORING_KURS.id, false)

		val status1 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		status1!!.type shouldBe DeltakerStatus.Type.VENTER_PA_OPPSTART
		verify(exactly = 0) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }

		val status2 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status2!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET
		status2.aarsak shouldBe DeltakerStatus.Aarsak.FATT_JOBB
		verify(exactly = 1) { publisherService.publish(DELTAKER_2.id, DataPublishType.DELTAKER, false) }

		val status3 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_4.id)
		status3!!.type shouldBe DeltakerStatus.Type.HAR_SLUTTET
		verify(exactly = 1) { publisherService.publish(DELTAKER_4.id, DataPublishType.DELTAKER, false) }
	}

	@Test
	fun `konverterStatuserForDeltakerePaaGjennomforing - fra lopende til kurs - setter riktig status pa deltakere`() {
		val gjennomforingInput = GJENNOMFORING_KURS.copy(sluttDato = LocalDate.now().plusYears(1))
		testDataRepository.insertGjennomforing(gjennomforingInput)
		testDataRepository.insertBruker(BRUKER_2)
		testDataRepository.insertBruker(BRUKER_4)
		testDataRepository.insertDeltaker(DELTAKER_1.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltaker(DELTAKER_2.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato.minusDays(4)))
		testDataRepository.insertDeltaker(DELTAKER_4.copy(gjennomforingId = GJENNOMFORING_KURS.id, sluttDato = gjennomforingInput.sluttDato))
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_2)
		testDataRepository.insertDeltakerStatus(DELTAKER_2_STATUS_1.copy(status = DeltakerStatus.Type.HAR_SLUTTET.name, aarsak = DeltakerStatus.Aarsak.FATT_JOBB.name))
		testDataRepository.insertDeltakerStatus(DELTAKER_4_STATUS_1.copy(status = DeltakerStatus.Type.HAR_SLUTTET.name, aarsak = null))

		every {
			gjennomforingService.getGjennomforing(any())
		} returns gjennomforingInput.toGjennomforing(TILTAK_1.toTiltak(), ARRANGOR_1.toArrangor())

		deltakerServiceImpl.konverterStatuserForDeltakerePaaGjennomforing(GJENNOMFORING_KURS.id, true)

		val status1 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_1.id)
		status1!!.type shouldBe DeltakerStatus.Type.VENTER_PA_OPPSTART
		verify(exactly = 0) { publisherService.publish(DELTAKER_1.id, DataPublishType.DELTAKER, false) }

		val status2 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_2.id)
		status2!!.type shouldBe DeltakerStatus.Type.AVBRUTT
		status2.aarsak shouldBe DeltakerStatus.Aarsak.FATT_JOBB
		verify(exactly = 1) { publisherService.publish(DELTAKER_2.id, DataPublishType.DELTAKER, false) }

		val status3 = deltakerStatusRepository.getStatusForDeltaker(DELTAKER_4.id)
		status3!!.type shouldBe DeltakerStatus.Type.FULLFORT
		verify(exactly = 1) { publisherService.publish(DELTAKER_4.id, DataPublishType.DELTAKER, false) }
	}

	val statusInsert = DeltakerStatusInsert(
		id = UUID.randomUUID(),
		deltakerId = deltakerId,
		type = DeltakerStatus.Type.DELTAR,
		aarsak = null,
		aarsaksbeskrivelse = null,
		gyldigFra = LocalDateTime.now().minusDays(7),
	)


	val deltaker = DeltakerUpsert(
		id = deltakerId,
		statusInsert = statusInsert,
		startDato = null,
		sluttDato = null,
		registrertDato = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
		dagerPerUke = null,
		prosentStilling = null,
		gjennomforingId = GJENNOMFORING_1.id,
		innsokBegrunnelse = null,
		innhold = null,
		kilde = Kilde.ARENA,
		forsteVedtakFattet = null,
		sistEndretAv = null,
		sistEndretAvEnhet = null
	)

}
