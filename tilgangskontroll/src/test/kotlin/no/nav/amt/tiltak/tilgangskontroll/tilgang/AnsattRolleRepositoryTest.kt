package no.nav.amt.tiltak.tilgangskontroll.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattCommand
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class AnsattRolleRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: AnsattRolleRepository

	lateinit var testDataRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = AnsattRolleRepository(NamedParameterJdbcTemplate(dataSource))

		testDataRepository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("opprettRolle skal opprette ny rolle") {
		val ansattId = UUID.randomUUID()

		testDataRepository.insertArrangorAnsatt(
			InsertArrangorAnsattCommand(
				id = ansattId,
				personligIdent = "",
				fornavn = "",
				etternavn = ""
			)
		)

		val id = UUID.randomUUID()

		repository.opprettRolle(id, ansattId, ARRANGOR_1.id, AnsattRolle.VEILEDER)

		val roller = repository.hentRoller(ansattId, ARRANGOR_1.id)

		roller shouldHaveSize 1

		val rolle = roller.first()

		rolle.id shouldBe id
		rolle.rolle shouldBe AnsattRolle.VEILEDER
		rolle.arrangorId shouldBe ARRANGOR_1.id
		rolle.ansattId shouldBe ansattId
	}

	test("hentRoller skal returnere roller") {
		val ansattId = UUID.randomUUID()

		testDataRepository.insertArrangorAnsatt(
			InsertArrangorAnsattCommand(
				id = ansattId,
				personligIdent = "",
				fornavn = "",
				etternavn = ""
			)
		)

		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.KOORDINATOR)
		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.VEILEDER)
		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_2.id, AnsattRolle.VEILEDER)

		val roller = repository.hentRoller(ansattId, ARRANGOR_1.id)

		roller.size shouldBe 2

		roller.any { it.rolle == AnsattRolle.KOORDINATOR } shouldBe true
		roller.any { it.rolle == AnsattRolle.VEILEDER } shouldBe true
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
