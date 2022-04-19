package no.nav.amt.tiltak.deltaker.repositories

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.nav_kontor.NavKontorRepository
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.NAV_KONTOR_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class NavKontorRepositoryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: NavKontorRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = NavKontorRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Get NAV-kontor med id bør kaste NoSuchElementException om det ikke eksisterer") {
		val id = UUID.randomUUID()

		val exception = shouldThrow<NoSuchElementException> {
			repository.get(id)
		}

		exception.message shouldBe "Kontor med id $id eksisterer ikke."
	}

	test("Legg til NAV-Kontor legger til og returnerer navkontor") {
		val enhetId = "ENHET_001"
		val navn = "ENHET_001_NAVN"

		val lagretKontor = repository.upsert(enhetId, navn)

		lagretKontor.id shouldNotBe null

		val hentetKontor = repository.get(lagretKontor.id)

		lagretKontor shouldBe hentetKontor
	}

	test("Endring av navn fører til endring av navn") {
		val oppdatertKontor = repository.upsert(NAV_KONTOR_1.enhet_id, "Nytt navn")

		oppdatertKontor.id shouldBe NAV_KONTOR_1.id
		oppdatertKontor.enhetId shouldBe NAV_KONTOR_1.enhet_id
		oppdatertKontor.navn shouldBe "Nytt navn"
	}

})
