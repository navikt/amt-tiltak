package no.nav.amt.tiltak.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

internal class DeltakerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: DeltakerRepository
	val tiltakInstansId = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
	val brukerId = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8")

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanDatabase(dataSource)
		DatabaseTestUtils.runScriptFile("/deltaker-repository_test-data.sql", dataSource)
	}

	test("Insert should insert Deltaker and return DeltakerDbo") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER
		val arenaStatus = "ARENA_STATUS"
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			brukerId,
			tiltakInstansId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			arenaStatus,
			dagerPerUke,
			prosentStilling
		)

		dbo shouldNotBe null
		dbo.id shouldNotBe null
		dbo.brukerId shouldBe brukerId
		dbo.brukerFornavn shouldBe "Bruker Fornavn"
		dbo.brukerEtternavn shouldBe "Bruker Etternavn"
		dbo.brukerFodselsnummer shouldBe "1"
		dbo.tiltakInstansId shouldBe tiltakInstansId
		dbo.deltakerOppstartsdato shouldBe oppstartDato
		dbo.deltakerSluttdato shouldBe sluttDato
		dbo.status shouldBe deltakerStatus
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
	}

	test("Update should update Deltaker and return the updated Deltaker") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER
		val arenaStatus = "ARENA_STATUS"
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			brukerId,
			tiltakInstansId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			arenaStatus,
			dagerPerUke,
			prosentStilling
		)

		val updatedOppstartsdato = LocalDate.now().plusDays(1)
		val updatedSluttdato = LocalDate.now().plusDays(14)
		val updatedStatus = Deltaker.Status.GJENNOMFORES

		val updated = dbo.update(updatedStatus, updatedOppstartsdato, updatedSluttdato)

		updated.status shouldBe UpdateStatus.UPDATED

		val updatedDbo = repository.update(updated.updatedObject!!)

		updatedDbo.id shouldBe dbo.id
		updatedDbo.deltakerOppstartsdato shouldBe updatedOppstartsdato
		updatedDbo.deltakerSluttdato shouldBe updatedSluttdato
		updatedDbo.status shouldBe updatedStatus
	}

	test("Get by id") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER
		val arenaStatus = "ARENA_STATUS"
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			brukerId,
			tiltakInstansId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			arenaStatus,
			dagerPerUke,
			prosentStilling
		)

		val gottenDbo = repository.get(dbo.id)

		gottenDbo shouldBe dbo
	}

	test("Get by BrukerId and Tiltaksinstans") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER
		val arenaStatus = "ARENA_STATUS"
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			brukerId,
			tiltakInstansId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			arenaStatus,
			dagerPerUke,
			prosentStilling
		)

		val gottenDbo = repository.get(dbo.brukerId, tiltakInstansId)

		gottenDbo shouldBe dbo
	}

	test("Get by Fodselsnummer and Tiltaksinstans") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER
		val arenaStatus = "ARENA_STATUS"
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			brukerId,
			tiltakInstansId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			arenaStatus,
			dagerPerUke,
			prosentStilling
		)

		val gottenDbo = repository.get("1", tiltakInstansId)

		gottenDbo shouldBe dbo
	}
})
