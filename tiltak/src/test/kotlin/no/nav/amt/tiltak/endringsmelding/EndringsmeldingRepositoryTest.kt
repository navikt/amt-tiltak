package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	lateinit var testRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		val template = NamedParameterJdbcTemplate(dataSource)
		rootLogger.level = Level.WARN

		testRepository = TestDataRepository(template)

		repository = EndringsmeldingRepository(
			template,
			TransactionTemplate(DataSourceTransactionManager(dataSource))
		)


		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("insertOgInaktiverStartDato - Ingen tidligere endringsmeldinger - inserter melding med alle verdier") {
		val now = LocalDate.now()
		val melding = repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		melding shouldNotBe null
		melding.deltakerId shouldBe DELTAKER_1.id
		melding.aktiv shouldBe true
		melding.opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
		melding.startDato shouldBe now
	}

	test("insertOgInaktiverStartDato - Det finnes flere endringsmeldinger - inserter melding og inaktiverer den gamle") {
		val idag = LocalDate.now()
		val melding1 = repository.insertOgInaktiverStartDato(idag, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		melding1.deltakerId shouldBe DELTAKER_1.id
		melding1.aktiv shouldBe true
		melding1.startDato shouldBe idag

		val nyDato = LocalDate.now().minusDays(1)
		val melding2 = repository.insertOgInaktiverStartDato(nyDato, DELTAKER_1.id, TestData.ARRANGOR_ANSATT_2.id)

		melding2 shouldNotBe null
		melding2.startDato shouldBe nyDato
		melding2.aktiv shouldBe true
		melding2.opprettetAvArrangorAnsattId shouldBe TestData.ARRANGOR_ANSATT_2.id

		val forrigeMelding = repository.get(melding1.id)

		melding1.copy(aktiv = false) shouldBe forrigeMelding
	}

	test("getByGjennomforing - en endringsmelding - henter endringsmelding") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 1
		meldinger[0].aktiv shouldBe true
	}

	test("getByGjennomforing - inaktiv endringsmelding - returnerer alle") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.insertOgInaktiverStartDato(now.minusDays(1), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforingId)

		meldinger.size shouldBe 2
		meldinger[0].aktiv shouldBe false
		meldinger[1].aktiv shouldBe true
	}

	test("getByDeltaker - henter endringsmelding") {
		repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_2.id, ARRANGOR_ANSATT_2.id)

		val meldinger = repository.getByDeltaker(DELTAKER_1.id)

		meldinger.size shouldBe 1
		meldinger[0].opprettetAvArrangorAnsattId shouldBe ARRANGOR_ANSATT_1.id
	}

	test("markerSomFerdig - skal sette aktiv=false og nav ansatt") {
		val melding =
			repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.markerSomFerdig(melding.id, NAV_ANSATT_1.id)

		val oppdatertMelding = repository.get(melding.id)

		oppdatertMelding.aktiv shouldBe false
		oppdatertMelding.ferdiggjortAvNavAnsattId shouldBe NAV_ANSATT_1.id
		oppdatertMelding.ferdiggjortTidspunkt!! shouldBeCloseTo ZonedDateTime.now()
	}

	test("skal returnere tom liste hvis ingen deltakerIder er sendt inn") {
		repository.getAktive(emptyList()) shouldBe emptyList()
	}

	test("skal hente aktive endringmseldinger for deltakere") {
		val localDate1 = LocalDate.parse("2022-09-04")
		val localDate2 = LocalDate.parse("2022-12-14")

		testRepository.insertEndringsmelding(EndringsmeldingInput(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_1.id,
			startDato = localDate1,
			aktiv = true,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
		))

		testRepository.insertEndringsmelding(EndringsmeldingInput(
			id = UUID.randomUUID(),
			deltakerId = DELTAKER_2.id,
			startDato = localDate2,
			aktiv = true,
			opprettetAvArrangorAnsattId = ARRANGOR_ANSATT_1.id,
		))

		val aktiveMeldinger = repository.getAktive(listOf(DELTAKER_1.id, DELTAKER_2.id))

		aktiveMeldinger shouldHaveSize 2
		aktiveMeldinger.any { it.deltakerId == DELTAKER_1.id && it.startDato == localDate1 }
		aktiveMeldinger.any { it.deltakerId == DELTAKER_2.id && it.startDato == localDate2 }
	}
})
