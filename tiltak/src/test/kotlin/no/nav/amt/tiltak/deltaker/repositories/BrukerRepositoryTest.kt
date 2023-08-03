package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID


class BrukerRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: BrukerRepository

	lateinit var testDataRespository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		testDataRespository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))
		repository = BrukerRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Upsert - ny bruker - skal inserte bruker") {
		val nyBruker = BRUKER_1.copy(
			id = UUID.randomUUID(),
			personIdent = "098761235"
		)

		repository.upsert(nyBruker.toModel())

		val faktiskBruker = repository.get(nyBruker.id)!!

		nyBruker.fornavn shouldBe faktiskBruker.fornavn
		nyBruker.mellomnavn shouldBe faktiskBruker.mellomnavn
		nyBruker.etternavn shouldBe faktiskBruker.etternavn
		nyBruker.personIdent shouldBe faktiskBruker.personIdent
	}

	test("upsert(Bruker) - bruker finnes - oppdaterer eksisterende bruker") {
		val originalBruker = repository.get(BRUKER_2.personIdent)

		val oppdatertBruker = BRUKER_2.copy(
			fornavn = "nytt",
			mellomnavn = null,
			etternavn = "navn",
			personIdent = "nytt fnr"
		)

		repository.upsert(oppdatertBruker.toModel())

		val nyBruker = repository.get(originalBruker!!.id)!!
		nyBruker.fornavn shouldBe oppdatertBruker.fornavn
		nyBruker.mellomnavn shouldBe oppdatertBruker.mellomnavn
		nyBruker.etternavn shouldBe oppdatertBruker.etternavn
		nyBruker.personIdent shouldBe oppdatertBruker.personIdent
		nyBruker.modifiedAt shouldBeAfter originalBruker.modifiedAt
	}

	test("get(ident) - bruker finnes ikke - returnerer null") {
		repository.get("234789") shouldBe null
	}


	test("slettBruker(id) - bruker finnes - sletter") {
		val bruker = BRUKER_1.copy(id = UUID.randomUUID(), personIdent = "678767")
		testDataRespository.insertBruker(bruker)

		repository.slettBruker(bruker.id)

		repository.get(bruker.id) shouldBe null
	}


})
