package no.nav.amt.tiltak.tiltak.services

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.repositories.SkjultDeltakerRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingServiceImpl
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

class GjennomforingServiceImplTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var gjennomforingRepository: GjennomforingRepository

	lateinit var testDataRepository: TestDataRepository

	lateinit var deltakerService: DeltakerService

	lateinit var gjennomforingService: GjennomforingService

	lateinit var arrangorService: ArrangorService

	lateinit var tiltakService: TiltakService

	lateinit var brukerService: BrukerService

	lateinit var navEnhetService: NavEnhetService

	lateinit var kafkaProducerService: KafkaProducerService

	lateinit var endringsmeldingService: EndringsmeldingServiceImpl

	lateinit var service: GjennomforingServiceImpl

	lateinit var skjulDeltakerRepository: SkjultDeltakerRepository


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

		skjulDeltakerRepository = mockk()

		gjennomforingService = GjennomforingServiceImpl(
			gjennomforingRepository,
			tiltakService,
			arrangorService
		)

		deltakerService = DeltakerServiceImpl(
			DeltakerRepository(parameterTemplate),
			DeltakerStatusRepository(parameterTemplate),
			brukerService,
			endringsmeldingService,
			skjulDeltakerRepository,
			gjennomforingService,
			transactionTemplate,
			kafkaProducerService
		)

		service = GjennomforingServiceImpl(
			gjennomforingRepository = gjennomforingRepository,
			tiltakService = tiltakService,
			arrangorService = arrangorService,
		)

		DbTestDataUtils.cleanDatabase(dataSource)
	}


	test("getGjennomforing - gjennomføring er kurs - returnerer gjennomføring") {
		val gjennomforingId = UUID.randomUUID()
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1.copy(id=gjennomforingId, erKurs = true))

		val tiltakInserted = TILTAK_1.toTiltak()
		val arrangorInserted = ARRANGOR_1.toArrangor()

		every { arrangorService.getArrangorById(ARRANGOR_1.id) } returns arrangorInserted
		every { tiltakService.getTiltakById(TILTAK_1.id) } returns tiltakInserted
		service.getGjennomforing(gjennomforingId).id shouldBe gjennomforingId

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

	test("upsert - arrangørId er endret - arrangørId oppdateres for gjennomføringen") {
		testDataRepository.insertNavEnhet(NAV_ENHET_1)
		testDataRepository.insertTiltak(TILTAK_1)
		testDataRepository.insertArrangor(ARRANGOR_1)
		testDataRepository.insertArrangor(ARRANGOR_2)
		testDataRepository.insertGjennomforing(GJENNOMFORING_1)
		val tiltakInserted = TILTAK_1.toTiltak()
		val oppdatertArrangor = ARRANGOR_2.toArrangor()

		every { arrangorService.getArrangorById(ARRANGOR_2.id) } returns oppdatertArrangor
		every { tiltakService.getTiltakById(TILTAK_1.id) } returns tiltakInserted

		service.upsert(
			GjennomforingUpsert(
				id = GJENNOMFORING_1.id,
				tiltakId = TILTAK_1.id,
				arrangorId = ARRANGOR_2.id,
				navn = GJENNOMFORING_1.navn,
				status = Gjennomforing.Status.GJENNOMFORES,
				startDato = GJENNOMFORING_1.startDato,
				sluttDato = GJENNOMFORING_1.sluttDato,
				navEnhetId = NAV_ENHET_1.id,
				lopenr = GJENNOMFORING_1.lopenr,
				opprettetAar = GJENNOMFORING_1.opprettetAar,
				erKurs = false
			)
		)

		val oppdatertGjennomforing = service.getGjennomforing(GJENNOMFORING_1.id)
		oppdatertGjennomforing.arrangor.id shouldBe ARRANGOR_2.id
	}

})
