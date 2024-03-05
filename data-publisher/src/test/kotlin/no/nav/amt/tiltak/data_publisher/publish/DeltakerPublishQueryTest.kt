package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.tiltak.Kilde
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class DeltakerPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val query = DeltakerPublishQuery(template)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get") {
		val input = db.createDeltaker()

		when (val data = query.get(input.id)) {
			is DeltakerPublishQuery.Result.OK -> data.result.id shouldBe input.id
			else -> fail("Should be ok, was $data")
		}
	}

	test("get - deltaker fra komet, uten vurdering, skal ikke publisere") {
		val input = db.createDeltaker(kilde = Kilde.KOMET)

		when (val data = query.get(input.id)) {
			is DeltakerPublishQuery.Result.DontPublish -> {}
			else -> fail("Should be ok, was $data")
		}
	}
})
