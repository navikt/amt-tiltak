package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.TestData.ARRANGOR_1_ID
import no.nav.amt.tiltak.test.database.TestData.ARRANGOR_ANSATT_2_FNR
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ArrangorerForAnsattRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var arrangorerForAnsattRepository: ArrangorerForAnsattRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		arrangorerForAnsattRepository = ArrangorerForAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Should get arrangorer for ansatt") {
		val arrangorer = arrangorerForAnsattRepository.query(ARRANGOR_ANSATT_2_FNR)

		arrangorer shouldHaveSize 1

		val arrangor = arrangorer.first()

		arrangor.id shouldBe ARRANGOR_1_ID
		arrangor.navn shouldBe "Tiltaksarrangør 1"
		arrangor.organisasjonsnummer shouldBe "111111111"
		arrangor.overordnetEnhetNavn shouldBe "Org Tiltaksarrangør 1"
		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe "911111111"
		arrangor.rolle shouldBe AnsattRolle.VEILEDER.name
	}

})
