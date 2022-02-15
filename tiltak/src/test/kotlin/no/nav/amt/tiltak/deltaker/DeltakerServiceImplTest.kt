package no.nav.amt.tiltak.deltaker

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.deltaker.service.DeltakerServiceImpl
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_3_FNR
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1_ID
import no.nav.amt.tiltak.tiltak.services.BrukerServiceImpl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.*

class DeltakerServiceImplTest {
	lateinit var deltakerRepository: DeltakerRepository
	lateinit var deltakerStatusRepository: DeltakerStatusRepository
	lateinit var deltakerServiceImpl: DeltakerServiceImpl
	lateinit var brukerRepository: BrukerRepository
	lateinit var brukerService: BrukerService

	val dataSource = SingletonPostgresContainer.getDataSource()
	val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)
	val deltakerId = UUID.randomUUID()

	@BeforeEach
	fun beforeEach() {
		brukerRepository = BrukerRepository(jdbcTemplate)

		brukerService = BrukerServiceImpl(brukerRepository, mockk(), mockk(), mockk(), mockk())
		deltakerRepository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		deltakerServiceImpl = DeltakerServiceImpl(
			deltakerRepository, deltakerStatusRepository,
			brukerService, TransactionTemplate(DataSourceTransactionManager(dataSource))
		)

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@AfterEach
	fun afterEach() {
		DatabaseTestUtils.cleanDatabase(dataSource)
	}

	@Test
	fun `upsertDeltaker - inserter ny deltaker`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)

		val nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)

		nyDeltaker shouldNotBe null

		val statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)

		statuser shouldHaveSize 1
		statuser.first().status shouldBe Deltaker.Status.DELTAR
		statuser.first().aktiv shouldBe true
	}

	@Test
	fun `upsertDeltaker - deltaker får ny status - oppdaterer status på deltaker`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)

		var nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		var statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 1

		val endretStatus = status.medNy(Deltaker.Status.HAR_SLUTTET, LocalDateTime.now())
		val endretDeltaker = deltaker.copy(statuser = endretStatus)

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, endretDeltaker)

		nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)
		val aktivStatus = statuser.first{ it.aktiv }

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 2
		aktivStatus.status shouldBe Deltaker.Status.HAR_SLUTTET

	}


	@Test
	fun `upsertDeltaker - deltaker får samme status igjen - oppdaterer ikke status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)
		var nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		var statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 1

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)

		nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)
		val aktivStatus = statuser.first{ it.aktiv }

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 1
		aktivStatus.status shouldBe Deltaker.Status.DELTAR

	}

	@Test
	fun `upsertDeltaker - deltaker får samme status på nytt etter opphold - oppdaterer status`() {

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)

		var nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		var statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 1

		val endretDeltaker = deltaker.copy(statuser = DeltakerStatuser.settAktivStatus(Deltaker.Status.HAR_SLUTTET))

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, endretDeltaker)

		nyDeltaker = deltakerRepository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)
		statuser = deltakerStatusRepository.getStatuserForDeltaker(deltakerId)
		val aktivStatus = statuser.first{ it.aktiv }

		nyDeltaker shouldNotBe null
		statuser shouldHaveSize 2
		aktivStatus.status shouldBe Deltaker.Status.HAR_SLUTTET

		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker.copy(statuser = DeltakerStatuser.settAktivStatus(Deltaker.Status.DELTAR)))

	}

	@Test
	fun `slettDeltaker - skal slette deltaker og status`() {
		deltakerServiceImpl.upsertDeltaker(BRUKER_3_FNR, GJENNOMFORING_1_ID, deltaker)

		deltakerStatusRepository.getStatuserForDeltaker(deltakerId) shouldHaveSize 1

		deltakerServiceImpl.slettDeltaker(deltakerId)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatuserForDeltaker(deltakerId) shouldHaveSize 0
	}

	val status = DeltakerStatuser.settAktivStatus(Deltaker.Status.DELTAR)

	val deltaker = Deltaker(
		id =  deltakerId,
		bruker = null,
		startDato = null,
		sluttDato = null,
		statuser =  status,
		registrertDato =  LocalDateTime.now(),
		dagerPerUke = null,
		prosentStilling = null
	)
}
