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
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_3
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
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

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		dbo shouldNotBe null
		dbo.id shouldBe id
		dbo.brukerId shouldBe BRUKER_3.id
		dbo.brukerFornavn shouldBe BRUKER_3.fornavn
		dbo.brukerEtternavn shouldBe BRUKER_3.etternavn
		dbo.brukerFodselsnummer shouldBe BRUKER_3.fodselsnummer
		dbo.gjennomforingId shouldBe GJENNOMFORING_1.id
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
				id = DELTAKER_1.id,
				bruker = Bruker(
					id = BRUKER_1.id,
					fornavn = "",
					etternavn = "",
					fodselsnummer = "",
					navEnhet = null
				),
				startDato = nyStartdato,
				sluttDato = nySluttdato,
				statuser = DeltakerStatuser(listOf(DeltakerStatus.nyAktiv(Deltaker.Status.DELTAR))),
				registrertDato = LocalDateTime.now(),
				gjennomforingId = UUID.randomUUID()
			)
		))

		updatedDeltaker.id shouldBe DELTAKER_1.id
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(dbo.brukerId, GJENNOMFORING_1.id)

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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		val gottenDbo = repository.get(BRUKER_3.fodselsnummer, GJENNOMFORING_1.id)

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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)

		deltakerStatusRepository.upsert(
			listOf(
				DeltakerStatusInsertDbo(
					id = UUID.randomUUID(),
					deltakerId = dbo.id,
					status = Deltaker.Status.DELTAR,
					gyldigFra = LocalDateTime.now().minusDays(5),
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusInsertDbo(
				id = UUID.randomUUID(),
				deltakerId = dbo.id,
				status = Deltaker.Status.DELTAR,
				gyldigFra = LocalDateTime.now().minusDays(5),
				aktiv = true
			)
			))

		val list = repository.potensieltHarSlutta().filter { it.id == BRUKER_3.id }

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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusInsertDbo(
				id = UUID.randomUUID(),
				deltakerId = dbo.id,
				status = Deltaker.Status.VENTER_PA_OPPSTART,
				gyldigFra = LocalDateTime.now().minusDays(5),
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato,
			sluttDato,
			dagerPerUke,
			prosentStilling,
			registrertDato
		)
		deltakerStatusRepository.upsert(
			listOf(DeltakerStatusInsertDbo(
				id = UUID.randomUUID(),
				deltakerId = dbo.id,
				status = Deltaker.Status.VENTER_PA_OPPSTART,
				gyldigFra = LocalDateTime.now().minusDays(5),
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
			BRUKER_3.id,
			GJENNOMFORING_1.id,
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
