package no.nav.amt.tiltak.endringsmelding.metrics

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

class EndringsmeldingMetricRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingMetricRepository

	lateinit var endringsmeldingRepository: EndringsmeldingRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val template = NamedParameterJdbcTemplate(dataSource)

		repository = EndringsmeldingMetricRepository(template)

		endringsmeldingRepository = EndringsmeldingRepository(template, TransactionTemplate(DataSourceTransactionManager(dataSource)))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("totaltAntallEndringsmeldinger - skal hente totalt antall endringsmeldinger") {
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.getMetrics()?.antallTotalt shouldBe 2
	}

	test("antallAktiveEndringsmeldinger - skal hente antall aktive endringsmeldinger") {
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.getMetrics()?.antallAktive shouldBe 1
	}

	test("antallAutomatiskFerdigEndringsmeldinger - skal hente antall automatisk ferdiggjorte endringsmeldinger") {
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		val endringsmelding = endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		endringsmeldingRepository.markerSomFerdig(endringsmelding.id, NAV_ANSATT_1.id)

		repository.getMetrics()?.automatiskFerdige shouldBe 2
	}

	test("antallManueltFerdigEndringsmeldinger - skal hente antall manuelt ferdiggjorte endringsmeldinger") {
		endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)
		val endringsmelding = endringsmeldingRepository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		endringsmeldingRepository.markerSomFerdig(endringsmelding.id, NAV_ANSATT_1.id)

		repository.getMetrics()?.manueltFerdige shouldBe 1
	}

})
