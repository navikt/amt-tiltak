package no.nav.amt.tiltak.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.testutils.DatabaseTestUtils
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.*

@Testcontainers
internal class DeltakerRepositoryTest : FunSpec({
	lateinit var template: NamedParameterJdbcTemplate
	lateinit var repository: DeltakerRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		template = DatabaseTestUtils.getDatabase("/deltaker-repository_test-data.sql")
		repository = DeltakerRepository(template)
	}

	test("Insert should insert Deltaker and return DeltakerDbo") {
		val brukerId = 1
		val tiltaksgjennomforing = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER

		val dbo = repository.insert(brukerId, tiltaksgjennomforing, oppstartDato, sluttDato, deltakerStatus)

		dbo shouldNotBe null
		dbo.internalId shouldNotBe null
		dbo.brukerInternalId shouldBe brukerId
		dbo.brukerFornavn shouldBe "Bruker Fornavn"
		dbo.brukerEtternavn shouldBe "Bruker Etternavn"
		dbo.brukerFodselsnummer shouldBe "1"
		dbo.tiltaksinstansInternalId shouldBe 1
		dbo.deltakerOppstartsdato shouldBe oppstartDato
		dbo.deltakerSluttdato shouldBe sluttDato
		dbo.status shouldBe deltakerStatus
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
	}

	test("Update should update Deltaker and return the updated Deltaker") {
		val brukerId = 1
		val tiltaksgjennomforing = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER

		val dbo = repository.insert(brukerId, tiltaksgjennomforing, oppstartDato, sluttDato, deltakerStatus)

		val updatedOppstartsdato = LocalDate.now().plusDays(1)
		val updatedSluttdato = LocalDate.now().plusDays(14)
		val updatedStatus = Deltaker.Status.GJENNOMFORES

		val updated = dbo.update(updatedStatus, updatedOppstartsdato, updatedSluttdato)

		updated.status shouldBe UpdateStatus.UPDATED

		val updatedDbo = repository.update(updated.updatedObject!!)

		updatedDbo.internalId shouldBe dbo.internalId
		updatedDbo.externalId shouldBe dbo.externalId
		updatedDbo.deltakerOppstartsdato shouldBe updatedOppstartsdato
		updatedDbo.deltakerSluttdato shouldBe updatedSluttdato
		updatedDbo.status shouldBe updatedStatus
	}

	test("Get by ExternalId") {
		val brukerId = 1
		val tiltaksgjennomforing = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER

		val dbo = repository.insert(brukerId, tiltaksgjennomforing, oppstartDato, sluttDato, deltakerStatus)

		val gottenDbo = repository.get(dbo.externalId)

		gottenDbo shouldBe dbo
	}

	test("Get by BrukerId and Tiltaksinstans") {
		val brukerId = 1
		val tiltaksgjennomforing = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER

		val dbo = repository.insert(brukerId, tiltaksgjennomforing, oppstartDato, sluttDato, deltakerStatus)

		val gottenDbo = repository.get(dbo.brukerInternalId, tiltaksgjennomforing)

		gottenDbo shouldBe dbo
	}

	test("Get by Fodselsnummer and Tiltaksinstans") {
		val brukerId = 1
		val tiltaksgjennomforing = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
		val oppstartDato = LocalDate.now().plusDays(7)
		val sluttDato = null
		val deltakerStatus = Deltaker.Status.NY_BRUKER

		val dbo = repository.insert(brukerId, tiltaksgjennomforing, oppstartDato, sluttDato, deltakerStatus)

		val gottenDbo = repository.get("1", tiltaksgjennomforing)

		gottenDbo shouldBe dbo
	}
})
