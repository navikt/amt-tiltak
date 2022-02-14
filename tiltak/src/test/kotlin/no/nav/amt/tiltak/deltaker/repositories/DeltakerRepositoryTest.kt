package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import no.nav.amt.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.TestData.BRUKER_1_ID
import no.nav.amt.tiltak.test.database.TestData.BRUKER_3_FNR
import no.nav.amt.tiltak.test.database.TestData.BRUKER_3_ID
import no.nav.amt.tiltak.test.database.TestData.DELTAKER_1_ID
import no.nav.amt.tiltak.test.database.TestData.GJENNOMFORING_1_ID
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

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = DeltakerRepository(NamedParameterJdbcTemplate(dataSource))
		deltakerStatusRepository = DeltakerStatusRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		dbo shouldNotBe null
		dbo.id shouldBe id
		dbo.brukerId shouldBe BRUKER_3_ID
		dbo.brukerFornavn shouldBe "Bruker 3 fornavn"
		dbo.brukerEtternavn shouldBe "Bruker 3 etternavn"
		dbo.brukerFodselsnummer shouldBe BRUKER_3_FNR
		dbo.gjennomforingId shouldBe GJENNOMFORING_1_ID
		dbo.startDato shouldBe startDato
		dbo.sluttDato shouldBe sluttDato
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
		dbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe registrertDato.truncatedTo(ChronoUnit.MINUTES)
	}

	test("Update should update Deltaker and return the updated Deltaker") {
		val nyStartdato = LocalDate.now().plusDays(1)
		val nySluttdato = LocalDate.now().plusDays(14)

		val updatedDeltaker = repository.update(DeltakerDbo(
			Deltaker(
				id = DELTAKER_1_ID,
				bruker = Bruker(
					id = BRUKER_1_ID,
					fornavn = "",
					etternavn = "",
					fodselsnummer = ""
				),
				startDato = nyStartdato,
				sluttDato = nySluttdato,
				statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(Deltaker.Status.DELTAR))),
				registrertDato = LocalDateTime.now()
			)
		))

		updatedDeltaker.id shouldBe DELTAKER_1_ID
		updatedDeltaker.startDato shouldBe nyStartdato
		updatedDeltaker.sluttDato shouldBe nySluttdato
	}

	test("Get by id") {
		val deltakerId = UUID.randomUUID()
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			deltakerId,
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(deltakerId)

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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.brukerId, GJENNOMFORING_1_ID)

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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(BRUKER_3_FNR, GJENNOMFORING_1_ID)

		gottenDbo shouldBe dbo
	}

	test("potensieltHarSlutta - status DELTAR og sluttdato passert - deltaker returneres") {
		val deltakerId = UUID.randomUUID()
		val startDato = LocalDate.now().minusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(10)
		val sluttDato = LocalDate.now().minusDays(2)
		val dagerPerUke = 2
		val prosentStilling = 20.0f

		val dbo = repository.insert(
			deltakerId,
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
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

		val list = repository.potensieltHarSlutta().filter { it.id == deltakerId }

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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
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

		val list = repository.potensieltHarSlutta().filter { it.id == BRUKER_3_ID }

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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
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
			BRUKER_3_ID,
			GJENNOMFORING_1_ID,
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
