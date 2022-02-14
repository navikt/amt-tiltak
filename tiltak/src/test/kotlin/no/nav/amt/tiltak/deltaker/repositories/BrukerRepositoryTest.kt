package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.TestData.BRUKER_1_FNR
import no.nav.amt.tiltak.test.database.TestData.VEILEDER_1_ID
import no.nav.amt.tiltak.test.database.TestData.VEILEDER_2_ID
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


class BrukerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: BrukerRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = BrukerRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Insert should insert bruker and return BrukerDbo") {
		val fodselsnummer = "64798632"
		val fornavn = "Per"
		val mellomnavn = null
		val etternavn = "Testersen"
		val telefonnummer = "74635462"
		val epost = "per.testersen@test.no"
		val ansvarligVeilederId = VEILEDER_1_ID
		val bruker = BrukerInsertDbo(fodselsnummer, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarligVeilederId, null)
		val dbo = repository.insert(bruker)

		dbo shouldNotBe null
		dbo.id shouldNotBe null
		dbo.fodselsnummer shouldBe fodselsnummer
		dbo.fornavn shouldBe fornavn
		dbo.etternavn shouldBe etternavn
		dbo.telefonnummer shouldBe telefonnummer
		dbo.epost shouldBe epost
		dbo.ansvarligVeilederId shouldBe ansvarligVeilederId
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
	}

	test("Get user that does not exist should be null") {
		repository.get("234789") shouldBe null
	}

	test("oppdaterVeileder should update veileder") {
		repository.get(BRUKER_1_FNR)?.ansvarligVeilederId shouldBe VEILEDER_1_ID

		repository.oppdaterVeileder(BRUKER_1_FNR, VEILEDER_2_ID)

		repository.get(BRUKER_1_FNR)?.ansvarligVeilederId shouldBe VEILEDER_2_ID
	}
})
