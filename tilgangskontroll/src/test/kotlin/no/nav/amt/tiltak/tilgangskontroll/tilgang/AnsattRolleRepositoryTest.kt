package no.nav.amt.tiltak.tilgangskontroll.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeCloseTo
import no.nav.amt.tiltak.test.database.DbUtils.shouldBeEqualTo
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorAnsattInput
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.ZonedDateTime
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
			ArrangorAnsattInput(
				id = ansattId,
				personligIdent = "",
				fornavn = "",
				etternavn = ""
			)
		)

		val id = UUID.randomUUID()
		val gyldigFra = ZonedDateTime.now().minusSeconds(10)
		val gyldigTil = ZonedDateTime.now().plusDays(1)

		repository.opprettRolle(id, ansattId, ARRANGOR_1.id, AnsattRolle.VEILEDER, gyldigFra, gyldigTil)

		val roller = repository.hentAktiveRoller(ansattId, ARRANGOR_1.id)

		roller shouldHaveSize 1

		val rolle = roller.first()

		rolle.id shouldBe id
		rolle.rolle shouldBe AnsattRolle.VEILEDER
		rolle.arrangorId shouldBe ARRANGOR_1.id
		rolle.ansattId shouldBe ansattId
		rolle.gyldigFra shouldBeEqualTo gyldigFra
		rolle.gyldigTil shouldBeEqualTo gyldigTil
	}

	test("hentAktiveRoller - skal returnere aktive roller") {
		val ansattId = UUID.randomUUID()

		testDataRepository.insertArrangorAnsatt(
			ArrangorAnsattInput(
				id = ansattId,
				personligIdent = "",
				fornavn = "",
				etternavn = ""
			)
		)
		val gyldigFra1 = ZonedDateTime.now().minusSeconds(5)
		val gyldigTil1 = ZonedDateTime.now().plusDays(1)

		val gyldigFra2 = ZonedDateTime.now().plusDays(5)
		val gyldigTil2 = ZonedDateTime.now().plusDays(8)

		val gyldigFra3 = ZonedDateTime.now().minusDays(5)
		val gyldigTil3 = ZonedDateTime.now().minusDays(3)

		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.KOORDINATOR, gyldigFra1, gyldigTil1)
		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.VEILEDER, gyldigFra2, gyldigTil2)
		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.VEILEDER, gyldigFra3, gyldigTil3)

		val roller = repository.hentAktiveRoller(ansattId, ARRANGOR_1.id)

		roller.size shouldBe 1

		val rolle = roller.first()

		rolle.rolle shouldBe AnsattRolle.KOORDINATOR
		rolle.arrangorId shouldBe ARRANGOR_1.id
		rolle.ansattId shouldBe ansattId
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

	test("deaktiverRolleHosArrangor - skal deaktivere roller hos arrangor") {
		val ansattId = UUID.randomUUID()

		testDataRepository.insertArrangorAnsatt(
			ArrangorAnsattInput(
				id = ansattId,
				personligIdent = "",
				fornavn = "",
				etternavn = ""
			)
		)

		val gyldigFra1 = ZonedDateTime.now().minusSeconds(5)
		val gyldigTil1 = ZonedDateTime.now().plusDays(1)

		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.KOORDINATOR, gyldigFra1, gyldigTil1)
		repository.opprettRolle(UUID.randomUUID(), ansattId, ARRANGOR_1.id, AnsattRolle.KOORDINATOR, gyldigFra1, gyldigTil1)
		repository.deaktiverRolleHosArrangor(ansattId, ARRANGOR_1.id, AnsattRolle.KOORDINATOR)

		repository.hentAktiveRoller(ansattId, ARRANGOR_1.id) shouldHaveSize 0

	}

})
