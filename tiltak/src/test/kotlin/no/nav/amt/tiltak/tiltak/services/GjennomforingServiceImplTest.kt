package no.nav.amt.tiltak.tiltak.services

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.BrukerServiceImpl
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

class GjennomforingServiceImplTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var gjennomforingRepository: GjennomforingRepository

	lateinit var testDataRepository: TestDataRepository

	lateinit var deltakerService: DeltakerService

	lateinit var arrangorService: ArrangorService

	lateinit var tiltakService: TiltakService

	lateinit var brukerService: BrukerServiceImpl

	lateinit var navEnhetService: NavEnhetService

	lateinit var kafkaProducerService: KafkaProducerService

	lateinit var endringsmeldingService: EndringsmeldingServiceImpl

	lateinit var service: GjennomforingServiceImpl


	beforeEach {
		val parameterTemplate = NamedParameterJdbcTemplate(dataSource)
		val transactionTemplate = TransactionTemplate(DataSourceTransactionManager(dataSource))

		gjennomforingRepository = GjennomforingRepository(parameterTemplate)

		testDataRepository = TestDataRepository(parameterTemplate)

		deltakerService = mockk()

		arrangorService = mockk()

		tiltakService = mockk()

		brukerService = mockk()

		navEnhetService = mockk()

		kafkaProducerService = mockk(relaxUnitFun = true)

		endringsmeldingService = mockk()

		service = GjennomforingServiceImpl(
			gjennomforingRepository = gjennomforingRepository,
			tiltakService = tiltakService,
			deltakerService = DeltakerServiceImpl(
				DeltakerRepository(parameterTemplate),
				DeltakerStatusRepository(parameterTemplate),
				brukerService,
				endringsmeldingService,
				mockk(),
				transactionTemplate,
				kafkaProducerService
			),
			arrangorService = arrangorService,
			transactionTemplate = transactionTemplate,
		)

		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("slettGjennomforing - skal slette gjennomføring") {
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1)

		testDataRepository.insertNavAnsatt(NAV_ANSATT_1)
		testDataRepository.insertBruker(BRUKER_1)
		testDataRepository.insertDeltaker(DELTAKER_1)
		testDataRepository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		every {
			endringsmeldingService.slettEndringsmeldingerForDeltaker(any())
		} returns Unit

		every {
			deltakerService.slettDeltaker(any())
		} returns Unit

		every {
			navEnhetService.getNavEnhet(any<UUID>())
		} returns NAV_ENHET_1.toNavEnhet()

		every {
			deltakerService.hentDeltakerePaaGjennomforing(GJENNOMFORING_1.id)
		} returns listOf(
			Deltaker(
				id = DELTAKER_1.id,
				fornavn = "Fornavn2",
				mellomnavn = null,
				etternavn = "Etternavn",
				personIdent = "12121231123",
				navEnhetId = null,
				navVeilederId = null,
				epost = "foo@bar.baz",
				telefonnummer = "1234",
				startDato = null,
				sluttDato = null,
				status = DeltakerStatus(
					UUID.randomUUID(),
					DeltakerStatus.Type.VENTER_PA_OPPSTART,
                    null,
					LocalDateTime.now(),
					LocalDateTime.now(),
					true
				),
				registrertDato = LocalDateTime.now(),
				dagerPerUke = null,
				prosentStilling = null,
				gjennomforingId = UUID.randomUUID(),
				erSkjermet = false
			))

		gjennomforingRepository.get(GJENNOMFORING_1.id) shouldNotBe null

		service.slettGjennomforing(GJENNOMFORING_1.id)

		gjennomforingRepository.get(GJENNOMFORING_1.id) shouldBe null
	}

	test("getGjennomforing - gjennomføring finnes - returnerer gjennomføring") {
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1)
		val tiltakInserted = TILTAK_1.toTiltak()
		val arrangorInserted = ARRANGOR_1.toArrangor()

		every { arrangorService.getArrangorById(ARRANGOR_1.id) } returns arrangorInserted
		every { tiltakService.getTiltakById(TILTAK_1.id) } returns tiltakInserted

		val gjennomforing = service.getGjennomforing(GJENNOMFORING_1.id)

		GJENNOMFORING_1.toGjennomforing(tiltakInserted, arrangorInserted) shouldBe gjennomforing
	}

	test("getByLopenummer - returnerer alle gjennomføringer, uansett status") {
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1)

		val avsluttetGjennomforing = GJENNOMFORING_1.copy(id = UUID.randomUUID(), status = Gjennomforing.Status.AVSLUTTET.name)
		testDataRepository.insertGjennomforing(avsluttetGjennomforing)

		val tiltakInserted = TILTAK_1.toTiltak()
		val arrangorInserted = ARRANGOR_1.toArrangor()
		val lopenr = GJENNOMFORING_1.lopenr

		every { arrangorService.getArrangorById(ARRANGOR_1.id) } returns arrangorInserted
		every { tiltakService.getTiltakById(TILTAK_1.id) } returns tiltakInserted

		val expectedIds = listOf(GJENNOMFORING_1.id, avsluttetGjennomforing.id)

		service.getByLopenr(lopenr).map { it.id } shouldContainAll  expectedIds
	}

})
