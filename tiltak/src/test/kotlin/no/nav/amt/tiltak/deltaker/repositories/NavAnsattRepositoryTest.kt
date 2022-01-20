package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


class NavAnsattRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: NavAnsattRepository

	val upsertCmd = UpsertNavAnsattCommand(
		navIdent = "test123",
		navn = "Fornavn Etternavn",
		telefonnummer = "7464635",
		epost = "fornavn.etternavn@nav.no",
	)

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = NavAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DatabaseTestUtils.cleanDatabase(dataSource)
	}

	test("Hent nav-ansatt som ikke eksisterer returnerer null") {
		repository.getNavAnsattWithIdent("DOES_NOT_EXIST") shouldBe null
	}

	test("Insert nav-ansatt så hent bør returnere nav-ansatt") {
		repository.upsert(upsertCmd)

		val storedDbo = repository.getNavAnsattWithIdent(upsertCmd.navIdent)

		storedDbo shouldNotBe null
		storedDbo!!.id shouldNotBe -1
		storedDbo.navIdent shouldBe upsertCmd.navIdent
		storedDbo.navn shouldBe upsertCmd.navn
		storedDbo.telefonnummer shouldBe upsertCmd.telefonnummer
		storedDbo.epost shouldBe upsertCmd.epost
	}

	test("Update nav-ansatt så hent bør returnere oppdatert nav-ansatt") {
		repository.upsert(upsertCmd)
		val storedDbo = repository.getNavAnsattWithIdent(upsertCmd.navIdent)

		repository.upsert(upsertCmd.copy(epost = null, telefonnummer = null))
		val updatedDbo = repository.getNavAnsattWithIdent(upsertCmd.navIdent)

		storedDbo!!.id shouldBe updatedDbo!!.id
		updatedDbo.epost shouldBe null
		updatedDbo.telefonnummer shouldBe null
	}


})
