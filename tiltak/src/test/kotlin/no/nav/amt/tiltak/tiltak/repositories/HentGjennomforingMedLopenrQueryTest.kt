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
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class HentGjennomforingMedLopenrQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: HentGjennomforingMedLopenrQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		query = HentGjennomforingMedLopenrQuery(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("skal hente gjennomføringer med løpenummer") {
		val gjennomforinger = query.query(GJENNOMFORING_1.lopenr!!)

		gjennomforinger shouldHaveSize 1

		val gjennomforing = gjennomforinger.first()

		gjennomforing.id shouldBe GJENNOMFORING_1.id
		gjennomforing.lopenr shouldBe GJENNOMFORING_1.lopenr
		gjennomforing.opprettetAr shouldBe GJENNOMFORING_1.opprettet_aar
		gjennomforing.navn shouldBe GJENNOMFORING_1.navn
		gjennomforing.arrangorOrganisasjonsnavn shouldBe ARRANGOR_1.overordnet_enhet_navn
		gjennomforing.arrangorVirksomhetsnavn shouldBe ARRANGOR_1.navn
	}

})
