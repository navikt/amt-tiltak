package no.nav.amt.tiltak.tilgangskontroll.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class AnsattRolleRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: AnsattRolleRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = AnsattRolleRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("hentArrangorIderForAnsatt skal returnere ider") {
		val ider = repository.hentArrangorIderForAnsatt(ARRANGOR_ANSATT_1.id)

		ider.size shouldBe 2
		ider.contains(ARRANGOR_1.id) shouldBe true
		ider.contains(ARRANGOR_2.id) shouldBe true
	}

	test("hentArrangorIderForAnsatt skal returnere tom liste hvis ansatt ikke finnes") {
		val ansattId = UUID.randomUUID()

		val ider = repository.hentArrangorIderForAnsatt(ansattId)

		ider.isEmpty() shouldBe true
	}

})
