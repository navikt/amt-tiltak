package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

internal class DeltakerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: DeltakerRepository
	val gjennomforingId = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
	val brukerId = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8")
	val fnr = "12345678910"

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/deltaker-repository_test-data.sql")
	}

	test("Insert should insert Deltaker and return DeltakerDbo") {
		val id = UUID.randomUUID()
		val oppstartDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			id,
			brukerId,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		dbo shouldNotBe null
		dbo.id shouldBe id
		dbo.brukerId shouldBe brukerId
		dbo.brukerFornavn shouldBe "Bruker Fornavn"
		dbo.brukerEtternavn shouldBe "Bruker Etternavn"
		dbo.brukerFodselsnummer shouldBe fnr
		dbo.gjennomforingId shouldBe gjennomforingId
		dbo.startDato shouldBe oppstartDato
		dbo.sluttDato shouldBe sluttDato
		dbo.status shouldBe deltakerStatus
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
		dbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe registrertDato.truncatedTo(ChronoUnit.MINUTES)
	}

	test("Update should update Deltaker and return the updated Deltaker") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)

		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val updatedOppstartsdato = LocalDate.now().plusDays(1)
		val updatedSluttdato = LocalDate.now().plusDays(14)
		val updatedStatus = Deltaker.Status.GJENNOMFORES

		val updated = dbo.update(updatedStatus, updatedOppstartsdato, updatedSluttdato)

		updated.status shouldBe UpdateStatus.UPDATED

		val updatedDbo = repository.update(updated.updatedObject!!)

		updatedDbo.id shouldBe dbo.id
		updatedDbo.startDato shouldBe updatedOppstartsdato
		updatedDbo.sluttDato shouldBe updatedSluttdato
		updatedDbo.status shouldBe updatedStatus
	}

	test("Get by id") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.id)

		gottenDbo shouldBe dbo
	}

	test("Get by BrukerId and Gjennomforing") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.brukerId, gjennomforingId)

		gottenDbo shouldBe dbo
	}

	test("Get by Fodselsnummer and Gjennomforing") {
		val oppstartDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			oppstartDato,
			sluttDato,
			deltakerStatus,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(fnr, gjennomforingId)

		gottenDbo shouldBe dbo
	}
})
