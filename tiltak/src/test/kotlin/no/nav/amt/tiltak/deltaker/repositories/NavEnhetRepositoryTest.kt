package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.nav_enhet.NavEnhetRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class NavEnhetRepositoryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: NavEnhetRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = NavEnhetRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("get() - skal hente enhet") {
		val enhet = repository.get(NAV_ENHET_1.id)

		enhet.id shouldBe NAV_ENHET_1.id
		enhet.enhetId shouldBe NAV_ENHET_1.enhetId
		enhet.navn shouldBe NAV_ENHET_1.navn
	}

	test("hentEnhet() - skal hente enhet") {
		val enhet = repository.hentEnhet(NAV_ENHET_1.enhetId)

		enhet?.id shouldBe NAV_ENHET_1.id
		enhet?.enhetId shouldBe NAV_ENHET_1.enhetId
		enhet?.navn shouldBe NAV_ENHET_1.navn
	}

	test("Get NAV-kontor med id bør kaste NoSuchElementException om det ikke eksisterer") {
		val id = UUID.randomUUID()

		val exception = shouldThrow<NoSuchElementException> {
			repository.get(id)
		}

		exception.message shouldBe "Enhet med id $id eksisterer ikke."
	}

	test("insert() - skal inserte ny nav enhet") {
		val id = UUID.randomUUID()
		val enhetId = "ENHET_001"
		val navn = "ENHET_001_NAVN"

		repository.insert(id, enhetId, navn)

		val hentetEnhet = repository.get(id)

		hentetEnhet.id shouldBe id
		hentetEnhet.enhetId shouldBe enhetId
		hentetEnhet.navn shouldBe navn
	}

})
