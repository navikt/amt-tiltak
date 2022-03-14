package no.nav.amt.navansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate


class NavAnsattRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: NavAnsattRepository

	val upsertCmd = NavAnsattDbo(
		navIdent = "test123",
		navn = "Fornavn Etternavn",
		telefonnummer = "7464635",
		epost = "fornavn.etternavn@nav.no",
	)

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = NavAnsattRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
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
		repository.upsert(NavAnsattDbo(
			navIdent = NAV_ANSATT_1.nav_ident,
			navn = "Nytt navn",
			epost = "Ny epost",
			telefonnummer = "Nytt telefonnummer",
		))

		val updatedDbo = repository.getNavAnsattWithIdent(NAV_ANSATT_1.nav_ident)

		updatedDbo!!.navn shouldBe "Nytt navn"
		updatedDbo.epost shouldBe "Ny epost"
		updatedDbo.telefonnummer shouldBe "Nytt telefonnummer"
	}


})
