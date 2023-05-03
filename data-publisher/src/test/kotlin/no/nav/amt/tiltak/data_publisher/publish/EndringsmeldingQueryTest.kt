package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class EndringsmeldingQueryTest: FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val query = EndringsmeldingPublishQuery(template)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get") {
		val input = db.createEndringsmelding()
		val data = query.get(input.id)

		data.id shouldBe input.id
	}

})
