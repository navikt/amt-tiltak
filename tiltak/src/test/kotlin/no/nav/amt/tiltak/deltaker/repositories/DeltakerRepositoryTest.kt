package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
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
	lateinit var deltakerStatusRepository: DeltakerStatusRepository
	val gjennomforingId = UUID.fromString("b3420940-5479-48c8-b2fa-3751c7a33aa2")
	val brukerId = UUID.fromString("23b04c3a-a36c-451f-b9cf-30b6a6b586b8")
	val fnr = "12345678910"
	val statusConverterMock = fun (id: UUID) =
		listOf(DeltakerStatusDbo(
			deltakerId = id,
			status = Deltaker.Status.DELTAR,
			endretDato = LocalDateTime.now(),
			aktiv = true)
		)


	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerRepository(NamedParameterJdbcTemplate(dataSource))
		deltakerStatusRepository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))

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

		val nyStartdato = LocalDate.now().plusDays(1)
		val nySluttdato = LocalDate.now().plusDays(14)

		val deltakerMedStatus = dbo.toDeltaker(statusConverterMock).let {
			it.oppdater(it.copy(startDato = nyStartdato, sluttDato = nySluttdato))
		}
		val deltakerToInsert = DeltakerDbo(deltakerMedStatus)


		deltakerMedStatus shouldNotBe dbo.toDeltaker(statusConverterMock)

		val insertedDeltaker = repository.update(deltakerToInsert)

		insertedDeltaker.id shouldBe dbo.id
		insertedDeltaker.startDato shouldBe nyStartdato
		insertedDeltaker.sluttDato shouldBe nySluttdato
	}

	test("Get by id") {
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

		val gottenDbo = repository.get(dbo.id)

		gottenDbo shouldBe dbo
	}

	test("Get by BrukerId and Gjennomforing") {
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

		val gottenDbo = repository.get(dbo.brukerId, gjennomforingId)

		gottenDbo shouldBe dbo
	}

	test("Get by Fodselsnummer and Gjennomforing") {
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

		val gottenDbo = repository.get(fnr, gjennomforingId)

		gottenDbo shouldBe dbo
	}

	test("potensieltHarSlutta - status DELTAR og sluttdato passert - deltaker returneres") {
		val startDato = LocalDate.now().minusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(10)
		val sluttDato = LocalDate.now().minusDays(2)
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
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusDbo(
					deltakerId = dbo.id,
					status = Deltaker.Status.DELTAR,
					endretDato = LocalDateTime.now().minusDays(5),
					aktiv = true
				)
		))

		val list = repository.potensieltHarSlutta()

		list shouldHaveSize 1
		list[0] shouldBe dbo
	}


	test("potensieltHarSlutta - status DELTAR og sluttdato ikke passert - deltaker returneres ikke") {
		val startDato = LocalDate.now().minusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(10)
		val sluttDato = LocalDate.now().plusDays(2)
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
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusDbo(
				deltakerId = dbo.id,
				status = Deltaker.Status.DELTAR,
				endretDato = LocalDateTime.now().minusDays(5),
				aktiv = true
			)
			))

		val list = repository.potensieltHarSlutta()

		list shouldHaveSize 0
	}


	test("potensieltDeltar - startdato passert og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres") {
		val startDato = LocalDate.now().minusDays(2)
		val registrertDato = LocalDateTime.now().minusDays(10)
		val sluttDato = LocalDate.now().plusDays(10)
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
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusDbo(
				deltakerId = dbo.id,
				status = Deltaker.Status.VENTER_PA_OPPSTART,
				endretDato = LocalDateTime.now().minusDays(5),
				aktiv = true
			)
			))

		val list = repository.potensieltDeltar()

		list shouldHaveSize 1
		list[0] shouldBe dbo
	}


	test("potensieltDeltar - startdato og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres ikke") {
		val startDato = LocalDate.now().plusDays(2)
		val registrertDato = LocalDateTime.now().minusDays(10)
		val sluttDato = LocalDate.now().plusDays(10)
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
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusDbo(
				deltakerId = dbo.id,
				status = Deltaker.Status.VENTER_PA_OPPSTART,
				endretDato = LocalDateTime.now().minusDays(5),
				aktiv = true
			)
			))

		val list = repository.potensieltDeltar()

		list shouldHaveSize 0
	}

	test("slettDeltaker skal slette deltaker") {
		val id = UUID.randomUUID()
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		repository.insert(
			id,
			brukerId,
			gjennomforingId,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		repository.get(id) shouldNotBe null

		repository.slettDeltaker(id)

		repository.get(id) shouldBe null
	}
})
