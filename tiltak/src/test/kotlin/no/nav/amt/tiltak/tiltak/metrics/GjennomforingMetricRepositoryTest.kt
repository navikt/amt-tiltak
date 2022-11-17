package no.nav.amt.tiltak.tiltak.metrics

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

internal class GjennomforingMetricRepositoryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var testRepository: TestDataRepository

	lateinit var repository: GjennomforingMetricRepository

	beforeEach {
		val template = NamedParameterJdbcTemplate(dataSource)

		testRepository = TestDataRepository(template)

		repository = GjennomforingMetricRepository(template)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("antallGjennomforingerPerType - skal måle antallet typer gjennomforinger som er tatt i bruk av arrangorer") {
		testRepository.deleteAllArrangorAnsattGjennomforingTilganger()
		val typerOgAntall = mapOf(
			"ARBFORB" to 1,
			"ARBRRHDAG" to 1,
			"AVKLARAG" to 1,
			"DIGIOPPARB" to 1,
			"GRUFAGYRKE" to 2,
			"VASV" to 1,
			"INDOPPFAG" to 3,
		)

		typerOgAntall.forEach {entry ->
			repeat(entry.value) {
				val tiltak = TILTAK_1.copy(id = UUID.randomUUID(), type = entry.key)
				val gjennomforing = GJENNOMFORING_1.copy(id = UUID.randomUUID(), tiltakId = tiltak.id)
				val tilgang = GJENNOMFORING_TILGANG_1.copy(id = UUID.randomUUID(), gjennomforingId = gjennomforing.id)
				testRepository.insertTiltak(tiltak)
				testRepository.insertGjennomforing(gjennomforing)
				testRepository.insertArrangorAnsattGjennomforingTilgang(tilgang)
			}
		}

		val res = repository.antallGjennomforingerPerType()
		res.forEach {
			typerOgAntall[it.type] shouldBe it.antall
		}
	}
})

