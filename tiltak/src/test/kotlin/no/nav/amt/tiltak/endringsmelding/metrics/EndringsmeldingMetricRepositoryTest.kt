package no.nav.amt.tiltak.endringsmelding.metrics

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.ENDRINGSMELDING_1_DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class EndringsmeldingMetricRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var testRepository: TestDataRepository

	lateinit var repository: EndringsmeldingMetricRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val template = NamedParameterJdbcTemplate(dataSource)

		testRepository = TestDataRepository(template)

		repository = EndringsmeldingMetricRepository(template)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		testRepository.deleteAllEndringsmeldinger()
	}

	test("totaltAntallEndringsmeldinger - skal hente totalt antall endringsmeldinger") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_2)

		repository.getMetrics()?.antallTotalt shouldBe 2
	}

	test("antallAktiveEndringsmeldinger - skal hente antall aktive endringsmeldinger") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTFORT
			)
		)

		repository.getMetrics()?.antallAktive shouldBe 1
	}

	test("antallAutomatiskFerdigEndringsmeldinger - skal hente antall endringsmeldinger som har status UTDATERT") {
		testRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTDATERT
			)
		)
		testRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTDATERT
			)
		)
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		repository.getMetrics()?.automatiskFerdige shouldBe 2
	}

	test("antallManueltFerdigEndringsmeldinger - skal hente antall endringsmeldinger med status UTFORT") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)
		testRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTFORT,
			)
		)

		repository.getMetrics()?.manueltFerdige shouldBe 1
	}

	test("getAntallEndringsmeldingerPerType - skal hente antall endringsmeldinger for hver type") {
		EndringsmeldingDbo.Type.values().forEach {
			testRepository.insertEndringsmelding(
				ENDRINGSMELDING_1_DELTAKER_1.copy(
					id = UUID.randomUUID(),
					type = it.name
				)
			)
		}
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		val result = repository.getAntallEndringsmeldingerPerType()
		result.size shouldBe EndringsmeldingDbo.Type.values().size
		result.forEach {
			if (it.type == ENDRINGSMELDING_1_DELTAKER_1.type) {
				it.antall shouldBe 2
			} else {
				it.antall shouldBe 1
			}
		}
	}

	test("antallEndringsmeldingerPerStatus - Skal vise antall endringsmeldinger per status") {
		testRepository.insertEndringsmelding(ENDRINGSMELDING_1_DELTAKER_1)

		testRepository.insertEndringsmelding(
			ENDRINGSMELDING_1_DELTAKER_1.copy(
				id = UUID.randomUUID(),
				status = Endringsmelding.Status.UTFORT
			)
		)

		val data = repository.getAntallEndringsmeldingerPerStatus()

		data[Endringsmelding.Status.AKTIV] shouldBe 1
		data[Endringsmelding.Status.UTFORT] shouldBe 1
	}

})
