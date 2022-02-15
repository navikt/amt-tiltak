package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2_ROLLE_1
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
		val arrangorer = arrangorerForAnsattRepository.query(ARRANGOR_ANSATT_2.personlig_ident)

		arrangorer shouldHaveSize 1

		val arrangor = arrangorer.first()

		arrangor.id shouldBe ARRANGOR_1.id
		arrangor.navn shouldBe ARRANGOR_1.navn
		arrangor.organisasjonsnummer shouldBe ARRANGOR_1.organisasjonsnummer
		arrangor.overordnetEnhetNavn shouldBe ARRANGOR_1.overordnet_enhet_navn
		arrangor.overordnetEnhetOrganisasjonsnummer shouldBe ARRANGOR_1.overordnet_enhet_organisasjonsnummer
		arrangor.rolle shouldBe ARRANGOR_ANSATT_2_ROLLE_1.rolle
	}

})
