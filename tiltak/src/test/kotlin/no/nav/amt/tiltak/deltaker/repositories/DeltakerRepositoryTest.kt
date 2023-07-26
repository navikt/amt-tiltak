package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerUpsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_3
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
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

	lateinit var testDataRepository: TestDataRepository

	val now = LocalDate.now().atStartOfDay()

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val jdbcTemplate = NamedParameterJdbcTemplate(dataSource)

		repository = DeltakerRepository(jdbcTemplate)
		deltakerStatusRepository = DeltakerStatusRepository(jdbcTemplate)
		testDataRepository = TestDataRepository(jdbcTemplate)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Insert should insert Deltaker and return DeltakerDbo") {
		val id = UUID.randomUUID()
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 2.5f
		val prosentStilling = 20.0f
		val begrunnelse = "begrunnelse"

		repository.upsert(
			DeltakerUpsertDbo(
				id,
				BRUKER_3.id,
				GJENNOMFORING_1.id,
				startDato,
				sluttDato,
				dagerPerUke = dagerPerUke,
				prosentStilling = prosentStilling,
				registrertDato = registrertDato,
				innsokBegrunnelse = begrunnelse
			)
		)
		val dbo = repository.get(id)

		dbo shouldNotBe null
		dbo!!.id shouldBe id
		dbo.fornavn shouldBe BRUKER_3.fornavn
		dbo.etternavn shouldBe BRUKER_3.etternavn
		dbo.personIdent shouldBe BRUKER_3.personIdent
		dbo.gjennomforingId shouldBe GJENNOMFORING_1.id
		dbo.startDato shouldBe startDato
		dbo.sluttDato shouldBe sluttDato
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
		dbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe registrertDato.truncatedTo(ChronoUnit.MINUTES)
		dbo.innsokBegrunnelse shouldBe begrunnelse
	}

	test("upsert - should update Deltaker and return the updated Deltaker") {
		val nyStartdato = LocalDate.now().plusDays(1)
		val nySluttdato = LocalDate.now().plusDays(14)
		val nyBegrunnelse = "ny begrunnelse"

		repository.upsert(DeltakerUpsertDbo(
			id = DELTAKER_1.id,
			brukerId = DELTAKER_1.brukerId,
			gjennomforingId = DELTAKER_1.gjennomforingId,
			startDato = nyStartdato,
			sluttDato = nySluttdato,
			registrertDato = LocalDateTime.now(),
			innsokBegrunnelse = nyBegrunnelse
		))
		val updatedDeltaker = repository.get(DELTAKER_1.id)

		updatedDeltaker shouldNotBe null
		updatedDeltaker!!.id shouldBe DELTAKER_1.id
		updatedDeltaker.startDato shouldBe nyStartdato
		updatedDeltaker.sluttDato shouldBe nySluttdato
		updatedDeltaker.innsokBegrunnelse shouldBe nyBegrunnelse
	}

	test("get - deltaker finnes - returnerer deltaker") {
		val insertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato =LocalDate.now().plusDays(7),
			sluttDato = null,
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = now
		)

		repository.upsert(insertDbo)

		val gottenDbo = repository.get(insertDbo.id)

		gottenDbo shouldNotBe null
		gottenDbo!!.id shouldBe insertDbo.id
		gottenDbo.gjennomforingId shouldBe insertDbo.gjennomforingId
		gottenDbo.startDato shouldBe insertDbo.startDato
		gottenDbo.sluttDato shouldBe insertDbo.sluttDato
		gottenDbo.dagerPerUke shouldBe insertDbo.dagerPerUke
		gottenDbo.prosentStilling shouldBe insertDbo.prosentStilling
		gottenDbo.registrertDato.truncatedTo(ChronoUnit.MINUTES) shouldBe insertDbo.registrertDato.truncatedTo(ChronoUnit.MINUTES)
	}

	test("get(brukerId, gjennomforing) - returnerer deltaker") {

		val insertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().plusDays(7),
			sluttDato = null,
			dagerPerUke = 2f,
			prosentStilling = 20.0f,
			registrertDato = now
		)

		repository.upsert(insertDbo)

		val gottenDbo = repository.get(insertDbo.brukerId, insertDbo.gjennomforingId)

		gottenDbo shouldNotBe null
		gottenDbo!!.id shouldBe insertDbo.id
		gottenDbo.gjennomforingId shouldBe insertDbo.gjennomforingId
		gottenDbo.startDato shouldBe insertDbo.startDato
		gottenDbo.sluttDato shouldBe insertDbo.sluttDato
		gottenDbo.dagerPerUke shouldBe insertDbo.dagerPerUke
		gottenDbo.prosentStilling shouldBe insertDbo.prosentStilling
		gottenDbo.registrertDato shouldBe insertDbo.registrertDato

	}

	test("get(fnr, gjennomforing) - skal returnere deltaker") {
		val startDato = LocalDate.now().plusDays(7)
		val registrertDato = LocalDateTime.now().minusDays(3)
		val sluttDato = null
		val dagerPerUke = 5f
		val prosentStilling = 20.0f
		val gjennomforing = GJENNOMFORING_1
		val bruker = BRUKER_3

		val insertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			bruker.id,
			gjennomforing.id,
			startDato = startDato,
			sluttDato = sluttDato,
			dagerPerUke = dagerPerUke,
			prosentStilling = prosentStilling,
			registrertDato = registrertDato
		)

		repository.upsert(insertDbo)

		val gottenDbo = repository.get(bruker.personIdent, gjennomforing.id)

		gottenDbo shouldNotBe null
		gottenDbo!!.gjennomforingId shouldBe gjennomforing.id


	}

	test("getDeltakere - finner og returnerer deltakere med id i liste") {
		val deltakere = repository.getDeltakere(listOf(DELTAKER_1.id, DELTAKER_2.id))
		deltakere shouldHaveSize 2
		deltakere.any { it.id == DELTAKER_1.id } shouldBe true
		deltakere.any { it.id == DELTAKER_2.id } shouldBe true
	}


	test("skalAvsluttes - status DELTAR og sluttdato passert - deltaker returneres") {
		val deltakerId = UUID.randomUUID()

		val deltakerInsertDbo = DeltakerUpsertDbo(
			deltakerId,
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().minusDays(7),
			sluttDato = LocalDate.now().minusDays(2),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = now.minusDays(10)
		)
		repository.upsert(deltakerInsertDbo)
		val deltaker = repository.get(deltakerInsertDbo.id)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltaker!!.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(5))

		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltHarSlutta = repository.sluttDatoPassert().filter { it.id == deltakerId }

		potensieltHarSlutta shouldHaveSize 1
		potensieltHarSlutta[0] shouldBe deltaker
	}

	test("skalAvsluttes - status DELTAR og sluttdato ikke passert - deltaker returneres ikke") {
		val deltakerInsertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().minusDays(7),
			sluttDato = LocalDate.now().plusDays(2),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = LocalDateTime.now().minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(5),
		)
		repository.upsert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltHarSlutta = repository.sluttDatoPassert().filter { it.id == BRUKER_3.id }

		potensieltHarSlutta shouldHaveSize 0
	}

	test("erPaaAvsluttetGjennomforing - status DELTAR og gjennomføring avslutttet - deltaker returneres") {
		val deltakerId = UUID.randomUUID()

		val deltakerInsertDbo = DeltakerUpsertDbo(
			deltakerId,
			BRUKER_3.id,
			GJENNOMFORING_2.id,
			startDato = LocalDate.now().minusDays(7),
			sluttDato = LocalDate.now().plusDays(2),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = LocalDateTime.now().minusDays(10)
		)
		repository.upsert(deltakerInsertDbo)
		val deltaker = repository.get(deltakerInsertDbo.id)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltaker!!.id,
			type = DeltakerStatus.Type.DELTAR,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(5))

		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltHarSlutta = repository.erPaaAvsluttetGjennomforing().filter { it.id == deltakerId }

		potensieltHarSlutta shouldHaveSize 1
		potensieltHarSlutta[0] shouldBe deltaker
	}


	test("skalHaStatusDeltar - startdato passert og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres") {

		val deltakerInsertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().minusDays(7),
			sluttDato = LocalDate.now().plusDays(2),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = now.minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = DeltakerStatus.Type.VENTER_PA_OPPSTART,
			aarsak = null,
			gyldigFra = now.minusDays(5)
		)
		repository.upsert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltDeltar = repository.skalHaStatusDeltar()

		potensieltDeltar shouldHaveSize 1
		potensieltDeltar[0].id shouldBe deltakerInsertDbo.id
		potensieltDeltar[0].gjennomforingId shouldBe deltakerInsertDbo.gjennomforingId
		potensieltDeltar[0].startDato shouldBe deltakerInsertDbo.startDato
		potensieltDeltar[0].sluttDato shouldBe deltakerInsertDbo.sluttDato
		potensieltDeltar[0].dagerPerUke shouldBe deltakerInsertDbo.dagerPerUke
		potensieltDeltar[0].prosentStilling shouldBe deltakerInsertDbo.prosentStilling
		potensieltDeltar[0].registrertDato shouldBe deltakerInsertDbo.registrertDato

	}


	test("skalHaStatusDeltar - startdato og sluttdato ikke passert og status VENTER_PA_OPPSTART - deltaker returneres ikke") {
		val deltakerInsertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().plusDays(2),
			sluttDato = LocalDate.now().plusDays(10),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = LocalDateTime.now().minusDays(10)
		)
		val statusInsertDbo = DeltakerStatusInsertDbo(
			id = UUID.randomUUID(),
			deltakerId = deltakerInsertDbo.id,
			type = DeltakerStatus.Type.VENTER_PA_OPPSTART,
			aarsak = null,
			gyldigFra = LocalDateTime.now().minusDays(5),
		)

		repository.upsert(deltakerInsertDbo)
		deltakerStatusRepository.insert(statusInsertDbo)

		val potensieltDeltar = repository.skalHaStatusDeltar()

		potensieltDeltar shouldHaveSize 0
	}

	test("slettDeltaker skal slette deltaker") {
		val deltakerInsertDbo = DeltakerUpsertDbo(
			UUID.randomUUID(),
			BRUKER_3.id,
			GJENNOMFORING_1.id,
			startDato = LocalDate.now().minusDays(7),
			sluttDato = LocalDate.now().plusDays(2),
			dagerPerUke = 2.5f,
			prosentStilling = 20.0f,
			registrertDato = LocalDateTime.now().minusDays(10)
		)

		repository.upsert(deltakerInsertDbo)
		repository.get(deltakerInsertDbo.id) shouldNotBe null
		repository.slettVeilederrelasjonOgDeltaker(deltakerInsertDbo.id)
		repository.get(deltakerInsertDbo.id) shouldBe null
	}

	test("hentDeltakere - skal hente deltakere med offset og limit") {
		testDataRepository.insertDeltaker(DELTAKER_1.copy(id = UUID.randomUUID()))

		val deltakere1 = repository.hentDeltakere(0, 1)

		deltakere1 shouldHaveSize 1

		val deltakere2 = repository.hentDeltakere(1, 2)

		val deltaker1Id = deltakere1[0].id

		deltakere2 shouldHaveSize 2
		deltakere2.any { it.id == deltaker1Id } shouldBe false
	}

})
