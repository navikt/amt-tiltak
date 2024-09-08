package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class DeltakerPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)
	val unleashService = mockk<UnleashService>()

	val query = DeltakerPublishQuery(template, unleashService)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns false
		val input = db.createDeltaker()

		when (val data = query.get(input.id, null)) {
			is DeltakerPublishQuery.Result.OK -> data.result.id shouldBe input.id
			else -> fail("Should be ok, was $data")
		}
	}

	test("get - deltaker som komet er master for, uten vurdering, skal ikke publisere") {
		every { unleashService.erKometMasterForTiltakstype(any()) } returns true
		val input = db.createDeltaker()

		when (val data = query.get(input.id, null)) {
			is DeltakerPublishQuery.Result.DontPublish -> {}
			else -> fail("Should be ok, was $data")
		}
	}
})
