package no.nav.amt.tiltak.ansatt

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DatabaseTestUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.database.data.commands.InsertArrangorAnsattGjennomforingTilgang
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GjennomforingTilgangRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var repository: GjennomforingTilgangRepository

	lateinit var testRepository: TestDataRepository

	beforeEach {
		val rootLogger: Logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
		rootLogger.level = Level.WARN

		val parameterTemplate = NamedParameterJdbcTemplate(dataSource)

		repository = GjennomforingTilgangRepository(parameterTemplate)
		testRepository = TestDataRepository(parameterTemplate)

		DatabaseTestUtils.cleanDatabase(dataSource)
	}

	test("hentGjennomforingerForAnsatt skal returnere ider") {
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)

		val id = UUID.randomUUID()
		val ansattId = ARRANGOR_ANSATT_1.id
		val gjennomforingId = GJENNOMFORING_1.id

		testRepository.insertArrangorAnsattGjennomforingTilgang(InsertArrangorAnsattGjennomforingTilgang(
			id, ansattId, gjennomforingId
		))

		val ider = repository.hentGjennomforingerForAnsatt(ansattId)

		ider.size shouldBe 1
		ider[0] shouldBe gjennomforingId
	}


})
