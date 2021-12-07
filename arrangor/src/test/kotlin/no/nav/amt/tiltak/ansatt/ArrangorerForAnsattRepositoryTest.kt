package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class ArrangorerForAnsattRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var arrangorerForAnsattRepository: ArrangorerForAnsattRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		arrangorerForAnsattRepository = ArrangorerForAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/get-arrangorer-for-ansatt-query-data.sql")
	}

	test("Should get arrangorer for no.nav.amt.tiltak.ansatt") {
		val arrangorer = arrangorerForAnsattRepository.query("123456789")

		val arrangor = arrangorer.first()

		arrangor.id shouldBe UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003")
		arrangor.navn shouldBe "VirkNavn"
		arrangor.organisasjonsnummer shouldBe "2"
		arrangor.overordnetEnhetNavn shouldBe "OrgNavn"
		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe "1"
		arrangor.rolle shouldBe AnsattRolle.KOORDINATOR.name
	}

})
