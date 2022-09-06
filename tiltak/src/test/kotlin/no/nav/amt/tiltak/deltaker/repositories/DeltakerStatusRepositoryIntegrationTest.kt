package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
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

class DeltakerStatusRepositoryIntegrationTest : IntegrationTestBase() {

	val lastweek = LocalDate.now().minusWeeks(1).atStartOfDay()
	val yesterday = LocalDate.now().minusDays(1).atStartOfDay()

	@Autowired
	lateinit var repository: DeltakerStatusRepository

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
	}

	@Test
	internal fun `insert - 2 statuser knyttet til deltaker - begge hentes`() {
		val deltakerCmd = TestData.createDeltakerInput(TestData.BRUKER_1, TestData.GJENNOMFORING_1)
		db.insertDeltaker(deltakerCmd)
		val status1 = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerCmd.id,
			type = Deltaker.Status.VENTER_PA_OPPSTART,
			gyldigFra = lastweek
		)
		val status2 = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerCmd.id,
			type = Deltaker.Status.DELTAR,
			gyldigFra = yesterday
		)
		val now = LocalDateTime.now()

		repository.insert(status1)
		repository.deaktiver(status1.id)
		repository.insert(status2)

		val persisted = repository.getStatuserForDeltaker(deltakerCmd.id)
		persisted shouldHaveSize 2

		persisted[0].copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			status1.id,
			status1.deltakerId,
			status1.type,
			status1.gyldigFra!!, now, false
		)

		persisted[1].copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			status2.id,
			status2.deltakerId,
			status2.type,
			status2.gyldigFra!!, now, true
		)
	}

	@Test
	internal fun `slettDeltakerStatus - skal slette status`() {
		val deltakerCmd = TestData.createDeltakerInput(TestData.BRUKER_1, TestData.GJENNOMFORING_1)
		db.insertDeltaker(deltakerCmd)
		val statusCmd = TestData.createStatusInput(deltakerCmd)
		db.insertDeltakerStatus(statusCmd)

		repository.getStatuserForDeltaker(deltakerCmd.id) shouldHaveSize 1
		repository.slettDeltakerStatus(deltakerCmd.id)
		repository.getStatuserForDeltaker(deltakerCmd.id) shouldHaveSize 0

	}
}
