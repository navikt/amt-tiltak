package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime

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

	test("get - flere skjult entries - tar siste entry") {
		val ansatt1 = db.createArrangorAnsatt()
		val ansatt2 = db.createArrangorAnsatt()
		val deltaker = db.createDeltaker()

		db.skjulDeltaker(deltaker.id, ansatt1.id, LocalDateTime.now().minusDays(7))
		db.skjulDeltaker(deltaker.id, ansatt1.id, LocalDateTime.now().minusDays(5))
		db.skjulDeltaker(deltaker.id, ansatt2.id, LocalDateTime.now().plusDays(5))

		when (val data = query.get(deltaker.id)) {
			is DeltakerPublishQuery.Result.OK -> data.result.skjult?.skjultAvAnsattId shouldBe ansatt2.id
			else -> fail("Should be ok, was $data")
		}
	}

	test("get - skjult entry, men gyldigTil er passert - gyldig") {
		val ansatt = db.createArrangorAnsatt()
		val deltaker = db.createDeltaker()

		db.skjulDeltaker(deltaker.id, ansatt.id, LocalDateTime.now().minusDays(1))

		when (val data = query.get(deltaker.id)) {
			is DeltakerPublishQuery.Result.OK -> data.result.skjult shouldBe null
			else -> fail("Should be ok, was $data")
		}
	}
})
