package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
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
	val statusConverterMock = fun (id: UUID) =
		listOf(DeltakerStatusDbo(
			deltakerId = id,
			status = Deltaker.Status.DELTAR,
			endretDato = LocalDate.now(),
			aktiv = true)
		)


	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/deltaker-repository_test-data.sql")
	}

	test("Insert should insert Deltaker and return DeltakerDbo") {
		val id = UUID.randomUUID()
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			id,
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
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
		dbo.startDato shouldBe startDato
		dbo.sluttDato shouldBe sluttDato
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
		dbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe registrertDato.truncatedTo(ChronoUnit.MINUTES)
	}

	test("Update should update Deltaker and return the updated Deltaker") {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)

		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val updatedStartDato = LocalDate.now().plusDays(1)
		val updatedSluttDato = LocalDate.now().plusDays(14)
		val updatedStatus = Deltaker.Status.DELTAR

		val updatedDeltaker = dbo.toDeltaker(statusConverterMock).updateStatus(updatedStatus, updatedStartDato, updatedSluttDato)
		val updated = DeltakerDbo(updatedDeltaker)


		updatedDeltaker shouldNotBe dbo.toDeltaker(statusConverterMock)

		val updatedDbo = repository.update(updated)

		updatedDbo.id shouldBe dbo.id
		updatedDbo.startDato shouldBe updatedStartDato
		updatedDbo.sluttDato shouldBe updatedSluttDato
	}

	test("Get by id") {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.id)

		gottenDbo shouldBe dbo
	}

	test("Get by BrukerId and Gjennomforing") {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.brukerId, gjennomforingId)

		gottenDbo shouldBe dbo
	}

	test("Get by Fodselsnummer and Gjennomforing") {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.VENTER_PA_OPPSTART
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			UUID.randomUUID(),
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(fnr, gjennomforingId)

		gottenDbo shouldBe dbo
	}
})
