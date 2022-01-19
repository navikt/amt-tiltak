package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

internal class DeltakerStatusRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val lastweek = LocalDate.now().minusWeeks(1)
	val yesterday = LocalDate.now().minusDays(1)
	val deltakerUUID = UUID.fromString("dc600c70-124f-4fe7-a687-b58439beb214")

	lateinit var repository: DeltakerStatusRepository

	beforeEach {

		repository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/deltaker-status-repository_test-data.sql")
	}

	test("upsert - 2 statuser knyttet til deltaker - begge hentes") {

		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = DELTAR, endretDato = yesterday, aktiv = false)
		)

		repository.upsert(statusesToPersist)

		val peristedStatuses = repository.getStatuserForDeltaker(deltakerUUID)
		peristedStatuses shouldHaveSize 2
		peristedStatuses shouldBe peristedStatuses

	}

	test("upsert - aktiv flagg endres - aktivflagg oppdatert") {

		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = DELTAR, endretDato = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val deaktiverte = repository.getStatuserForDeltaker(deltakerUUID).map { it.copy(aktiv = false) }
		repository.upsert(deaktiverte)

		repository.getStatuserForDeltaker(deltakerUUID).map { it.aktiv } shouldNotContain true

	}


	test("upsert - ny status - legges til i listen") {

		val statusesToPersist = listOf(
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = VENTER_PA_OPPSTART, endretDato = lastweek, aktiv = false),
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = DELTAR, endretDato = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val utvidet = repository.getStatuserForDeltaker(deltakerUUID) +
			DeltakerStatusDbo(deltakerId = deltakerUUID, status = HAR_SLUTTET, endretDato = yesterday, aktiv = true)
		repository.upsert(utvidet)

		repository.getStatuserForDeltaker(deltakerUUID) shouldHaveSize 3

	}

})

