package no.nav.amt.tiltak.tiltak.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class HentTiltaksoversiktQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: HentTiltaksoversiktQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentTiltaksoversiktQuery(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal hente gjennomføringer") {
		val gjennomforinger = query.query(listOf(GJENNOMFORING_1.id, GJENNOMFORING_2.id))

		gjennomforinger shouldHaveSize 2

		val gjennomforing = gjennomforinger.find { it.id == GJENNOMFORING_1.id }!!

		gjennomforing.id shouldBe GJENNOMFORING_1.id
		gjennomforing.navn shouldBe GJENNOMFORING_1.navn
		gjennomforing.arrangorOrganisasjonsnavn shouldBe ARRANGOR_1.overordnet_enhet_navn
		gjennomforing.arrangorVirksomhetsnavn shouldBe ARRANGOR_1.navn
	}

})
