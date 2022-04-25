package no.nav.amt.tiltak.tilgangskontroll.tilgang

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
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

		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("opprettTilgang skal opprette tilgang") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)

		repository.opprettTilgang(UUID.randomUUID(), ARRANGOR_ANSATT_1.id, GJENNOMFORING_1.id)

		val gjennomforingIder = repository.hentGjennomforingerForAnsatt(ARRANGOR_ANSATT_1.id)

		gjennomforingIder.first() shouldBe GJENNOMFORING_1.id
	}

	test("hentGjennomforingerForAnsatt skal returnere ider") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertNavEnhet(NAV_ENHET_2)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangor(ARRANGOR_2)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)
		testRepository.insertGjennomforing(GJENNOMFORING_2)

		val ansattId = ARRANGOR_ANSATT_1.id
		val gjennomforing1Id = GJENNOMFORING_1.id
		val gjennomforing2Id = GJENNOMFORING_2.id

		testRepository.insertArrangorAnsattGjennomforingTilgang(InsertArrangorAnsattGjennomforingTilgang(
			UUID.randomUUID(), ansattId, gjennomforing1Id
		))

		testRepository.insertArrangorAnsattGjennomforingTilgang(InsertArrangorAnsattGjennomforingTilgang(
			UUID.randomUUID(), ansattId, gjennomforing2Id
		))

		val ider = repository.hentGjennomforingerForAnsatt(ansattId)

		ider.size shouldBe 2
		ider.contains(gjennomforing1Id) shouldBe true
		ider.contains(gjennomforing2Id) shouldBe true
	}

	test("hentGjennomforingerForAnsattHosArrangor skal returnere ider") {
		testRepository.insertNavEnhet(NAV_ENHET_1)
		testRepository.insertNavEnhet(NAV_ENHET_2)
		testRepository.insertArrangor(ARRANGOR_1)
		testRepository.insertArrangor(ARRANGOR_2)
		testRepository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		testRepository.insertTiltak(TILTAK_1)
		testRepository.insertGjennomforing(GJENNOMFORING_1)
		testRepository.insertGjennomforing(GJENNOMFORING_2)

		val ansattId = ARRANGOR_ANSATT_1.id

		testRepository.insertArrangorAnsattGjennomforingTilgang(InsertArrangorAnsattGjennomforingTilgang(
			UUID.randomUUID(), ansattId, GJENNOMFORING_1.id
		))

		testRepository.insertArrangorAnsattGjennomforingTilgang(InsertArrangorAnsattGjennomforingTilgang(
			UUID.randomUUID(), ansattId, GJENNOMFORING_2.id
		))

		val ider = repository.hentGjennomforingerForAnsattHosArrangor(ansattId, GJENNOMFORING_2.arrangor_id)

		ider.size shouldBe 1
		ider[0] shouldBe GJENNOMFORING_2.id
	}

})
