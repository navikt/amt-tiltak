package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertEndringsmeldingCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

class AntallAktiveEndringsmeldingerQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: AntallAktiveEndringsmeldingerQuery

	lateinit var testRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = AntallAktiveEndringsmeldingerQuery(NamedParameterJdbcTemplate(dataSource))

		testRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal hente antall endringsmeldinger") {
		val gjennomforingIder = listOf(GJENNOMFORING_1.id, GJENNOMFORING_2.id)

		val deltaker1 = TestData.DELTAKER_1.copy(
			id = UUID.randomUUID(),
			gjennomforingId = GJENNOMFORING_1.id
		)

		val deltaker2 = TestData.DELTAKER_2.copy(
			id = UUID.randomUUID(),
			gjennomforingId = GJENNOMFORING_2.id
		)

		testRepository.insertDeltaker(deltaker1)
		testRepository.insertDeltaker(deltaker2)

		testRepository.insertEndringsmelding(InsertEndringsmeldingCommand(
			id = UUID.randomUUID(),
			deltakerId = deltaker1.id,
			startDato = LocalDate.now(),
			aktiv = true,
			opprettetAvArrangorAnsattId = TestData.ARRANGOR_ANSATT_1.id,
		))

		testRepository.insertEndringsmelding(InsertEndringsmeldingCommand(
			id = UUID.randomUUID(),
			deltakerId = deltaker2.id,
			startDato = LocalDate.now(),
			aktiv = true,
			opprettetAvArrangorAnsattId = TestData.ARRANGOR_ANSATT_1.id,
		))


		val antallMeldinger = query.query(gjennomforingIder)

		antallMeldinger shouldHaveSize 2
		antallMeldinger.any { it.gjennomforingId == GJENNOMFORING_1.id && it.antallMeldinger == 1 } shouldBe true
		antallMeldinger.any { it.gjennomforingId == GJENNOMFORING_2.id && it.antallMeldinger == 1 } shouldBe true
	}

	test("skal returnere tom liste hvis ingen gjennomføringIder blir sendt inn") {
		query.query(emptyList()) shouldHaveSize 0
	}

})
