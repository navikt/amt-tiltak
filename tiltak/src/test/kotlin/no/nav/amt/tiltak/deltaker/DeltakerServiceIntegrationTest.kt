package no.nav.amt.tiltak.deltaker

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatusInsert
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerUpsert
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.repositories.DeltakerRepository
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatusRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class DeltakerServiceIntegrationTest : IntegrationTestBase() {

	@Autowired
	lateinit var deltakerService: DeltakerService

	@Autowired
	lateinit var deltakerRepository: DeltakerRepository

	@Autowired
	lateinit var deltakerStatusRepository: DeltakerStatusRepository

	val deltakerId = UUID.randomUUID()

	private val deltaker = DeltakerUpsert(
		id = deltakerId,
		startDato = null,
		sluttDato = null,
		registrertDato = LocalDateTime.now(),
		dagerPerUke = null,
		prosentStilling = null,
		gjennomforingId = TestData.GJENNOMFORING_1.id,
		innsokBegrunnelse = null
	)


	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
	}

	@Test
	internal fun `upsertDeltaker - inserter ny deltaker`() {
		deltakerService.upsertDeltaker(TestData.BRUKER_1.fodselsnummer, deltaker)
		val nyDeltaker = deltakerRepository.get(TestData.BRUKER_1.fodselsnummer, deltaker.gjennomforingId)


		nyDeltaker shouldNotBe null
		nyDeltaker!!.id shouldBe deltaker.id
		nyDeltaker.gjennomforingId shouldBe deltaker.gjennomforingId

	}

	@Test
	internal fun `insertStatus - ingester status`() {
		deltakerService.upsertDeltaker(TestData.BRUKER_1.fodselsnummer, deltaker)
		val nyDeltaker = deltakerRepository.get(TestData.BRUKER_1.fodselsnummer, TestData.GJENNOMFORING_1.id)
		val now = LocalDate.now().atStartOfDay()

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			gyldigFra = now
		)

		deltakerService.insertStatus(statusInsertDbo)

		val statusEtterEndring = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		statusEtterEndring shouldNotBe null
		statusEtterEndring!!.copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			id = statusInsertDbo.id,
			deltakerId = nyDeltaker.id,
			status = statusInsertDbo.type,
			gyldigFra = statusInsertDbo.gyldigFra!!,
			opprettetDato = now,
			aktiv = true
		)

	}

	@Test
	fun `insertStatus - deltaker får samme status igjen - oppdaterer ikke status`() {

		deltakerService.upsertDeltaker(TestData.BRUKER_1.fodselsnummer, deltaker)
		val nyDeltaker = deltakerRepository.get(TestData.BRUKER_1.fodselsnummer, TestData.GJENNOMFORING_1.id)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			gyldigFra = LocalDateTime.now()
		)

		deltakerService.insertStatus(statusInsertDbo)

		val status1 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		deltakerService.insertStatus(statusInsertDbo)

		val status2 = deltakerStatusRepository.getStatusForDeltaker(nyDeltaker.id)

		status2 shouldBe status1

	}

	@Test
	fun `insertStatus - deltaker ny status - setter ny og deaktiverer den gamle`() {
		val deltakerCmd = TestData.createDeltakerInput(TestData.BRUKER_1, TestData.GJENNOMFORING_1)
		db.insertDeltaker(deltakerCmd)

		val nyDeltaker = deltakerRepository.get(deltakerCmd.brukerId, deltakerCmd.gjennomforingId)

		nyDeltaker shouldNotBe null

		val statusInsertDbo = DeltakerStatusInsert(
			id = UUID.randomUUID(),
			deltakerId = nyDeltaker!!.id,
			type = Deltaker.Status.IKKE_AKTUELL,
			gyldigFra = LocalDateTime.now()
		)

		deltakerService.insertStatus(statusInsertDbo)

		deltakerService.insertStatus(
			statusInsertDbo.copy(
				id = UUID.randomUUID(),
				type = Deltaker.Status.VENTER_PA_OPPSTART
			)
		)

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
			gyldigFra = LocalDateTime.now().minusDays(2)
		)

		deltakerService.upsertDeltaker(TestData.BRUKER_1.fodselsnummer, deltaker)
		deltakerService.insertStatus(statusInsertDbo)

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldNotBe null

		deltakerService.slettDeltaker(deltakerId)

		deltakerRepository.get(deltakerId) shouldBe null

		deltakerStatusRepository.getStatusForDeltaker(deltakerId) shouldBe null
	}


}
