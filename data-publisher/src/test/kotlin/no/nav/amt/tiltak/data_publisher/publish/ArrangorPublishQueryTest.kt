package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.UUID

class ArrangorPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val amtArrangorClient: AmtArrangorClient = mockk()

	val query = ArrangorPublishQuery(template, amtArrangorClient)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		every { amtArrangorClient.hentArrangor(any()) } returns AmtArrangorClient.ArrangorMedOverordnetArrangor(UUID.randomUUID(), "", "", null)
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get - Arrangor - returnerer riktig Arrangør") {
		val input = db.createArrangor()
		every { amtArrangorClient.hentArrangor(any()) } returns AmtArrangorClient.ArrangorMedOverordnetArrangor(
			id = UUID.randomUUID(),
			navn = "Parent",
			organisasjonsnummer = input.overordnetEnhetOrganisasjonsnummer!!,
			overordnetArrangor = null
		)

		val data = query.get(input.id)
		data.id shouldBe input.id
		data.organisasjonsnummer shouldBe input.organisasjonsnummer
	}
})
