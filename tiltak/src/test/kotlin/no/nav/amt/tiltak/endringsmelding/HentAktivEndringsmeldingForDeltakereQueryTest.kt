package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.EndringsmeldingInput
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate
import java.util.*

class HentAktivEndringsmeldingForDeltakereQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: HentAktivEndringsmeldingForDeltakereQuery

	lateinit var testRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentAktivEndringsmeldingForDeltakereQuery(NamedParameterJdbcTemplate(dataSource))

		testRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal returnere tom liste hvis ingen deltakerIder er sendt inn") {
		query.query(emptyList()) shouldBe emptyList()
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

		val aktiveMeldinger = query.query(listOf(DELTAKER_1.id, DELTAKER_2.id))

		aktiveMeldinger shouldHaveSize 2
		aktiveMeldinger.any { it.deltakerId == DELTAKER_1.id && it.startDato == localDate1 }
		aktiveMeldinger.any { it.deltakerId == DELTAKER_2.id && it.startDato == localDate2 }
	}

})
