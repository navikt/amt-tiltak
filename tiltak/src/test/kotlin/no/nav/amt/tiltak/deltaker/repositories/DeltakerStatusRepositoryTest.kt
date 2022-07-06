package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.createDeltakerInput
import no.nav.amt.tiltak.test.database.data.TestData.createStatusInput
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.TestDataSeeder
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class DeltakerStatusRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val lastweek = LocalDate.now().minusWeeks(1).atStartOfDay()
	val yesterday = LocalDate.now().minusDays(1).atStartOfDay()

	lateinit var repository: DeltakerStatusRepository
	lateinit var testDataRepository: TestDataRepository
	beforeEach {

		repository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))
		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource, TestDataSeeder::insertMinimum)
	}

	test("insert - 2 statuser knyttet til deltaker - begge hentes") {
		val deltakerCmd = createDeltakerInput(BRUKER_1, GJENNOMFORING_1)
		testDataRepository.insertDeltaker(deltakerCmd)
		val status1 = DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = deltakerCmd.id, type = VENTER_PA_OPPSTART, gyldigFra = lastweek)
		val status2 = DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = deltakerCmd.id, type = DELTAR, gyldigFra = yesterday)
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
			status1.gyldigFra!!, now, false )

		persisted[1].copy(opprettetDato = now) shouldBe DeltakerStatusDbo(
			status2.id,
			status2.deltakerId,
			status2.type,
			status2.gyldigFra!!, now, true)
	}


	test("slettDeltakerStatus - skal slette status") {
		val deltakerCmd = createDeltakerInput(BRUKER_1, GJENNOMFORING_1)
		testDataRepository.insertDeltaker(deltakerCmd)
		val statusCmd = createStatusInput(deltakerCmd)
		testDataRepository.insertDeltakerStatus(statusCmd)

		repository.getStatuserForDeltaker(deltakerCmd.id) shouldHaveSize 1
		repository.slettDeltakerStatus(deltakerCmd.id)
		repository.getStatuserForDeltaker(deltakerCmd.id) shouldHaveSize 0
	}

})
