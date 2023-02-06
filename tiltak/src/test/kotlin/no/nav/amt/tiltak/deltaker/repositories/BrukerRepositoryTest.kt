package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.deltaker.dbo.BrukerUpsertDbo
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


class BrukerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: BrukerRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = BrukerRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Insert - ny bruker - skal inserte bruker") {
		val fodselsnummer = "64798632"
		val fornavn = "Per"
		val mellomnavn = null
		val etternavn = "Testersen"
		val telefonnummer = "74635462"
		val epost = "per.testersen@test.no"
		val ansvarligVeilederId = NAV_ANSATT_1.id
		val bruker = BrukerUpsertDbo(fodselsnummer, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarligVeilederId, null, false)
		val dbo = repository.upsert(bruker)

		dbo shouldNotBe null
		dbo.id shouldNotBe null
		dbo.personIdent shouldBe fodselsnummer
		dbo.fornavn shouldBe fornavn
		dbo.etternavn shouldBe etternavn
		dbo.telefonnummer shouldBe telefonnummer
		dbo.epost shouldBe epost
		dbo.ansvarligVeilederId shouldBe ansvarligVeilederId
		dbo.erSkjermet shouldBe false
		dbo.createdAt shouldNotBe null
		dbo.modifiedAt shouldNotBe null
	}
	test("upsert - bruker finnes - oppdaterer eksisterende bruker") {
		val originalBruker = repository.get(BRUKER_2.personIdent)
		originalBruker shouldNotBe null

		val bruker = BrukerUpsertDbo(BRUKER_2.personIdent,
			"fornavn",
			"mellomnavn",
			"etternavn",
			"telefonnummer",
			"epost",
			NAV_ANSATT_1.id,
			NAV_ENHET_2.id,
			false
		)

		val nyBruker = repository.upsert(bruker)

		nyBruker.fornavn shouldBe bruker.fornavn
		nyBruker.etternavn shouldBe bruker.etternavn
		nyBruker.telefonnummer shouldBe bruker.telefonnummer
		nyBruker.epost shouldBe bruker.epost
		nyBruker.ansvarligVeilederId shouldBe bruker.ansvarligVeilederId
		nyBruker.modifiedAt shouldBeAfter originalBruker!!.modifiedAt
	}

	test("Get user that does not exist should be null") {
		repository.get("234789") shouldBe null
	}

	test("oppdaterVeileder should update veileder") {
		repository.get(BRUKER_1.personIdent)?.ansvarligVeilederId shouldBe NAV_ANSATT_1.id

		repository.oppdaterVeileder(BRUKER_1.personIdent, NAV_ANSATT_2.id)

		repository.get(BRUKER_1.personIdent)?.ansvarligVeilederId shouldBe NAV_ANSATT_2.id
	}

	test("settSkjermet should update bruker") {
		repository.get(BRUKER_1.personIdent)?.erSkjermet shouldBe false

		repository.settSkjermet(BRUKER_1.personIdent, true)

		repository.get(BRUKER_1.personIdent)?.erSkjermet shouldBe true
	}

	test("getBrukere - ingen brukere har ident - returnerer tom liste") {
		repository.getBrukere(listOf("309390")) shouldBe emptyList()
	}

	test("getBrukere - bruker med ident finnes - returnerer bruker") {
		val brukere = repository.getBrukere(listOf(BRUKER_1.personIdent))
		brukere.size shouldBe 1
		brukere.first().id shouldBe BRUKER_1.id
	}

	test("getBrukere - flere brukere med ident finnes - returnerer bruker") {
		val brukere = repository.getBrukere(listOf(BRUKER_1.personIdent, BRUKER_2.personIdent))
		brukere.size shouldBe 2
		brukere.find { it.id == BRUKER_1.id } shouldNotBe null
		brukere.find { it.id == BRUKER_2.id } shouldNotBe null

	}

	test("oppdaterIdenter - bruker finnes med annen ident - oppdaterer") {
		val nyIdent = "1234"
		val identer = listOf(BRUKER_1.personIdent, nyIdent)

		repository.oppdaterIdenter(BRUKER_1.id, nyIdent, identer)
		val bruker = repository.get(BRUKER_1.id)

		bruker!!.personIdent shouldBe nyIdent
		bruker.identer shouldBe identer
	}

})
