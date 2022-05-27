package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equality.shouldBeEqualToComparingFieldsExcept
import io.kotest.matchers.equality.shouldNotBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = EndringsmeldingRepository(
			NamedParameterJdbcTemplate(dataSource),
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
		melding.opprettetAvId shouldBe ARRANGOR_ANSATT_1.id
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
		melding2.opprettetAvId shouldBe TestData.ARRANGOR_ANSATT_2.id

		val forrigeMelding = repository.get(melding1.id)

		melding1.copy(aktiv = false) shouldBe forrigeMelding
	}

	test("getByGjennomforing - en endringsmelding - henter endringsmelding") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforing_id)

		meldinger.size shouldBe 1
		meldinger[0].aktiv shouldBe true
	}

	test("getByGjennomforing - inaktiv endringsmelding - returnerer alle") {
		val now = LocalDate.now()
		repository.insertOgInaktiverStartDato(now, DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.insertOgInaktiverStartDato(now.minusDays(1), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		val meldinger = repository.getByGjennomforing(DELTAKER_1.gjennomforing_id)

		meldinger.size shouldBe 2
		meldinger[0].aktiv shouldBe false
		meldinger[1].aktiv shouldBe true
	}

	test("getByDeltaker - henter endringsmelding") {
		repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_2.id, ARRANGOR_ANSATT_2.id)

		val meldinger = repository.getByDeltaker(DELTAKER_1.id)

		meldinger.size shouldBe 1
		meldinger[0].opprettetAvId shouldBe ARRANGOR_ANSATT_1.id
	}

	test("markerSomFerdig - skal sette aktiv=false og nav ansatt") {
		val godkjentTidspunkt = LocalDateTime.now()
		val melding =
			repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.markerSomFerdig(melding.id, NAV_ANSATT_1.id, godkjentTidspunkt)

		val oppdatertMelding = repository.get(melding.id)

		oppdatertMelding.aktiv shouldBe false
		oppdatertMelding.godkjentAvNavAnsatt shouldBe NAV_ANSATT_1.id
		oppdatertMelding.godkjentTidspunkt!!.toLocalDate() shouldBe godkjentTidspunkt.toLocalDate()
		ChronoUnit.MILLIS.between(oppdatertMelding.godkjentTidspunkt, godkjentTidspunkt) shouldBe 0
		ChronoUnit.SECONDS.between(oppdatertMelding.godkjentTidspunkt, godkjentTidspunkt) shouldBe 0
		ChronoUnit.MINUTES.between(oppdatertMelding.godkjentTidspunkt, godkjentTidspunkt) shouldBe 0
		ChronoUnit.HOURS.between(oppdatertMelding.godkjentTidspunkt, godkjentTidspunkt) shouldBe 0
	}

})
