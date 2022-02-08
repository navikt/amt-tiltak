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

class AnsattRolleRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: AnsattRolleRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = AnsattRolleRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabase(dataSource, "/arrangor-ansatt-med-roller.sql")
	}

	test("hentArrangorIderForAnsatt skal returnere ider") {
		val ansattId = UUID.fromString("6321c7dc-6cfb-47b0-b566-32979be5041f")

		val ider = repository.hentArrangorIderForAnsatt(ansattId)

		ider.size shouldBe 2
		ider.contains(UUID.fromString("8a37bce6-3bc1-11ec-8d3d-0242ac130003")) shouldBe true
		ider.contains(UUID.fromString("71ca161f-f1d4-468c-a041-e72b8bbc0612")) shouldBe true
	}

	test("hentArrangorIderForAnsatt skal returnere tom liste hvis ansatt ikke finnes") {
		val ansattId = UUID.randomUUID()

		val ider = repository.hentArrangorIderForAnsatt(ansattId)

		ider.isEmpty() shouldBe true
	}

})
