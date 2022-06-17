package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

	test("Get NAV-kontor med id bør kaste NoSuchElementException om det ikke eksisterer") {
		val id = UUID.randomUUID()

		val exception = shouldThrow<NoSuchElementException> {
			repository.get(id)
		}

		exception.message shouldBe "Enhet med id $id eksisterer ikke."
	}

	test("Legg til NAV-Kontor legger til og returnerer nav enhet") {
		val enhetId = "ENHET_001"
		val navn = "ENHET_001_NAVN"

		val lagretEnhet = repository.upsert(enhetId, navn)

		lagretEnhet.id shouldNotBe null

		val hentetEnhet = repository.get(lagretEnhet.id)

		lagretEnhet shouldBe hentetEnhet
	}

	test("Endring av navn fører til endring av navn") {
		val oppdatertEnhet = repository.upsert(NAV_ENHET_1.enhetId, "Nytt navn")

		oppdatertEnhet.id shouldBe NAV_ENHET_1.id
		oppdatertEnhet.enhetId shouldBe NAV_ENHET_1.enhetId
		oppdatertEnhet.navn shouldBe "Nytt navn"
	}

})
