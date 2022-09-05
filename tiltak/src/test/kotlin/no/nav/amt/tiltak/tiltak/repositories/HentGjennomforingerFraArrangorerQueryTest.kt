package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_3
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class HentGjennomforingerFraArrangorerQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: HentGjennomforingerFraArrangorerQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentGjennomforingerFraArrangorerQuery(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		TestDataRepository(NamedParameterJdbcTemplate(dataSource)).insertGjennomforing(GJENNOMFORING_3)
	}

	test("skal hente alle gjennomføringer fra arrangører") {
		val arrangorIder = listOf(ARRANGOR_1.id, ARRANGOR_2.id)

		val result = query.query(arrangorIder)

		result shouldHaveSize 2

		result.any { it == GJENNOMFORING_1.id }
		result.any { it == GJENNOMFORING_3.id }
	}


})
