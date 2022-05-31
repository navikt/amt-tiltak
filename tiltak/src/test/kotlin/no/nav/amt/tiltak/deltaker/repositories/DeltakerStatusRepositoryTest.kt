package no.nav.amt.tiltak.deltaker.repositories

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker.Status.*
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime
import java.util.*

internal class DeltakerStatusRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()
	val lastweek = LocalDateTime.now().minusWeeks(1)
	val yesterday = LocalDateTime.now().minusDays(1)

	lateinit var repository: DeltakerStatusRepository

	beforeEach {

		repository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("upsert - 2 statuser knyttet til deltaker - begge hentes") {
		val statusesToPersist = listOf(
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = VENTER_PA_OPPSTART, gyldigFra = lastweek, aktiv = false),
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = DELTAR, gyldigFra = yesterday, aktiv = false)
		)

		repository.upsert(statusesToPersist)

		val peristedStatuses = repository.getStatuserForDeltaker(DELTAKER_1.id)
		peristedStatuses shouldHaveSize 3
		peristedStatuses shouldBe peristedStatuses

	}

	test("upsert - aktiv flagg endres - aktivflagg oppdatert") {

		val statusesToPersist = listOf(
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = VENTER_PA_OPPSTART, gyldigFra = lastweek, aktiv = false),
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = DELTAR, gyldigFra = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val deaktiverte = repository.getStatuserForDeltaker(DELTAKER_1.id).map {
			DeltakerStatusInsertDbo(it.id, it.deltakerId, it.status, aktiv = false, gyldigFra = it.gyldigFra)
		}
		repository.upsert(deaktiverte)

		repository.getStatuserForDeltaker(DELTAKER_1.id).map { it.aktiv } shouldNotContain true

	}

	test("upsert - ny status - legges til i listen") {

		val statusesToPersist = listOf(
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = VENTER_PA_OPPSTART, gyldigFra = lastweek, aktiv = false),
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = DELTAR, gyldigFra = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)
		val utvidet = repository.getStatuserForDeltaker(DELTAKER_1.id) +
			DeltakerStatusDbo(deltakerId = DELTAKER_1.id, status = HAR_SLUTTET, gyldigFra = yesterday, aktiv = true, opprettetDato = LocalDateTime.now())
		repository.upsert(utvidet.map { it.toInsertableDbo() })
		repository.getStatuserForDeltaker(DELTAKER_1.id) shouldHaveSize 4

	}

	test("slettDeltakerStatus - skal slette status") {
		val statusesToPersist = listOf(
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = VENTER_PA_OPPSTART, gyldigFra = lastweek, aktiv = false),
			DeltakerStatusInsertDbo(id = UUID.randomUUID(), deltakerId = DELTAKER_1.id, status = DELTAR, gyldigFra = yesterday, aktiv = true)
		)

		repository.upsert(statusesToPersist)

		repository.getStatuserForDeltaker(DELTAKER_1.id) shouldHaveSize 3

		repository.slettDeltakerStatus(DELTAKER_1.id)

		repository.getStatuserForDeltaker(DELTAKER_1.id) shouldHaveSize 0
	}

	fun DeltakerStatusDbo.toInsertDbo() = DeltakerStatusInsertDbo(
		id = id,
		deltakerId = deltakerId,
		status = status,
		gyldigFra = gyldigFra,
		aktiv = aktiv
	)

})

private fun DeltakerStatusDbo.toInsertableDbo() = DeltakerStatusInsertDbo(
	id = id,
	deltakerId = deltakerId,
	status = status,
	gyldigFra = gyldigFra,
	aktiv = aktiv
)
