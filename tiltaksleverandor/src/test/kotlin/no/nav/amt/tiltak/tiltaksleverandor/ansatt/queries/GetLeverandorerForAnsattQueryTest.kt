package no.nav.amt.tiltak.tiltaksleverandor.ansatt.queries

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.tiltaksleverandor.controllers.dto.AnsattRolle
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetLeverandorerForAnsattQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var getLeverandorerForAnsattQuery: GetLeverandorerForAnsattQuery

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		getLeverandorerForAnsattQuery = GetLeverandorerForAnsattQuery(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/get-leverandorer-for-ansatt-query-data.sql")
	}

	test("Should get leverandorer for ansatt") {
		val leverandorer = getLeverandorerForAnsattQuery.query("123456789")

		val leverandor = leverandorer.first()

		leverandor.id shouldBe UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003")
		leverandor.navn shouldBe "VirkNavn"
		leverandor.organisasjonsnummer shouldBe "2"
		leverandor.overordnetEnhetNavn shouldBe "OrgNavn"
		leverandor.overordnetEnhetOrganisasjonsnummer shouldBe "1"
		leverandor.rolle shouldBe AnsattRolle.KOORDINATOR.name
	}

})
