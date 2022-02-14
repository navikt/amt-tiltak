package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.TestData.DELTAKER_1_ID
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime

internal class DeltakerStatusRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val lastweek = LocalDateTime.now().minusWeeks(1)
	val yesterday = LocalDateTime.now().minusDays(1)

	lateinit var repository: DeltakerStatusRepository

	beforeEach {

		repository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("upsert - 2 statuser knyttet til deltaker - begge hentes") {
		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = DELTAR, endretDato = yesterday, aktiv = false)
		)

		repository.upsert(statusesToPersist)

		val peristedStatuses = repository.getStatuserForDeltaker(DELTAKER_1_ID)
		peristedStatuses shouldHaveSize 3
		peristedStatuses shouldBe peristedStatuses

	}

	test("upsert - aktiv flagg endres - aktivflagg oppdatert") {

		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = DELTAR, endretDato = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val deaktiverte = repository.getStatuserForDeltaker(DELTAKER_1_ID).map { it.copy(aktiv = false) }
		repository.upsert(deaktiverte)

		repository.getStatuserForDeltaker(DELTAKER_1_ID).map { it.aktiv } shouldNotContain true

	}


	test("upsert - ny status - legges til i listen") {

		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = DELTAR, endretDato = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val utvidet = repository.getStatuserForDeltaker(DELTAKER_1_ID) +
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = HAR_SLUTTET, endretDato = yesterday, aktiv = true)
		repository.upsert(utvidet)

		repository.getStatuserForDeltaker(DELTAKER_1_ID) shouldHaveSize 4

	}

	test("slettDeltakerStatus - skal slette status") {
		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = DELTAKER_1_ID, status = DELTAR, endretDato = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)

		repository.getStatuserForDeltaker(DELTAKER_1_ID) shouldHaveSize 3

		repository.slettDeltakerStatus(DELTAKER_1_ID)

		repository.getStatuserForDeltaker(DELTAKER_1_ID) shouldHaveSize 0
	}

})

