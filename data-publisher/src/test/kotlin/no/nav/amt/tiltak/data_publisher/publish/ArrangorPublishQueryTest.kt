package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.clients.amt_enhetsregister.Virksomhet
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ArrangorPublishQueryTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val enhetsregisterClient: EnhetsregisterClient = mockk()

	val query = ArrangorPublishQuery(template, enhetsregisterClient)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		every { enhetsregisterClient.hentVirksomhet(any()) } returns Virksomhet("", "", null, null)
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("get - Arrangor - 0 deltakerlister - returnerer riktig Arrangør") {
		val input = db.createArrangor()
		every { enhetsregisterClient.hentVirksomhet(any()) } returns Virksomhet("Parent", input.overordnetEnhetOrganisasjonsnummer!!, null, null)

		val data = query.get(input.id)
		data.id shouldBe input.id
		data.organisasjonsnummer shouldBe input.organisasjonsnummer
	}

	test("get - Arrangor - 1 deltakerliste - returnerer riktig") {
		val arrangorInput = db.createArrangor()
		val deltakerlisteInput = db.createDeltakerliste(
			arrangorId = arrangorInput.id
		)

		every { enhetsregisterClient.hentVirksomhet(any()) } returns Virksomhet("Parent", arrangorInput.overordnetEnhetOrganisasjonsnummer!!, null, null)


		val data = query.get(arrangorInput.id)

		data.id shouldBe arrangorInput.id
		data.deltakerlister.size shouldBe 1
		data.deltakerlister.first() shouldBe deltakerlisteInput.id
	}


})
