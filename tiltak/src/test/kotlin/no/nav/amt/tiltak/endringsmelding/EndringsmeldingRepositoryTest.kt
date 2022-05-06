package no.nav.amt.tiltak.endringsmelding

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDate

class EndringsmeldingRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: EndringsmeldingRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		repository = EndringsmeldingRepository(NamedParameterJdbcTemplate(dataSource))

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("markerSomFerdig - skal sette aktiv=false og nav ansatt") {
		val melding =
			repository.insertOgInaktiverStartDato(LocalDate.now(), DELTAKER_1.id, ARRANGOR_ANSATT_1.id)

		repository.markerSomFerdig(melding.id, NAV_ANSATT_1.id)

		val oppdatertMelding = repository.get(melding.id)

		oppdatertMelding.aktiv shouldBe false
		oppdatertMelding.godkjentAvNavAnsatt shouldBe NAV_ANSATT_1.id
	}

})
