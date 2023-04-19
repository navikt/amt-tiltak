package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ArrangorPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val query = ArrangorPublishQuery(template)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get - Arrangor - 0 deltakerlister - returnerer riktig Arrangør") {
		val input = db.createArrangor()
		val data = query.get(input.id)

		data.id shouldBe input.id
		data.organisasjon.nummer shouldBe input.organisasjonsnummer
		data.overordnetOrganisasjon?.nummer shouldBe input.overordnetEnhetOrganisasjonsnummer
	}

	test("get - Arrangor - 1 deltakerliste - returnerer riktig") {
		val arrangorInput = db.createArrangor()
		val deltakerlisteInput = db.createDeltakerliste(
			arrangorId = arrangorInput.id
		)

		val data = query.get(arrangorInput.id)

		data.id shouldBe arrangorInput.id
		data.deltakerlister.size shouldBe 1
		data.deltakerlister.first() shouldBe deltakerlisteInput.id
	}


})
