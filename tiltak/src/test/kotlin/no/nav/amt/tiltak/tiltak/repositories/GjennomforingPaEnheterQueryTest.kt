package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_KONTOR_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_KONTOR_2
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

internal class GjennomforingPaEnheterQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: GjennomforingerPaEnheterQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val template = NamedParameterJdbcTemplate(dataSource)

		query = GjennomforingerPaEnheterQuery(template)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal hente gjennomføringer på enheter") {
		val gjennomforinger = query.query(listOf(NAV_KONTOR_1.id, NAV_KONTOR_2.id))

		gjennomforinger shouldHaveSize 2
		gjennomforinger.any { it.id == GJENNOMFORING_1.id } shouldBe true
		gjennomforinger.any { it.id == GJENNOMFORING_2.id } shouldBe true
	}

})
