package no.nav.amt.tiltak.deltaker.controllers

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import org.junit.jupiter.api.Test
import java.util.*

class TiltakarrangorDeltakerControllerIntegrationTest : IntegrationTestBase() {

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 401 when not authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}"
		)

		response.code shouldBe 401
	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should perform authorization check`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${UUID.randomUUID()}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 404

	}

	@Test
	fun `hentTiltakDeltakerDetaljer() should return 200 when authenticated`() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		poaoTilgangClient.addErSkjermetResponse(mapOf(BRUKER_1.fodselsnummer to false))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/tiltak-deltaker/${DELTAKER_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		val expectedBody = """
			{"id":"dc600c70-124f-4fe7-a687-b58439beb214","fornavn":"Bruker 1 fornavn","mellomnavn":null,"etternavn":"Bruker 1 etternavn","fodselsnummer":"12345678910","telefonnummer":"73404782","epost":"bruker1@example.com","navEnhet":{"navn":"NAV Testheim"},"navVeileder":{"navn":"Vashnir Veiledersen","telefon":"88776655","epost":"vashnir.veiledersen@nav.no"},"erSkjermetPerson":false,"startDato":"2022-02-13","sluttDato":"2030-02-14","registrertDato":"2022-02-13T12:12:00","status":{"type":"DELTAR","endretDato":"2022-02-13T00:00:00"},"gjennomforing":{"id":"b3420940-5479-48c8-b2fa-3751c7a33aa2","navn":"Tiltaksgjennomforing1","startDato":"2022-02-01","sluttDato":"2050-12-30","status":"GJENNOMFORES","tiltak":{"tiltakskode":"AMO","tiltaksnavn":"Tiltak1"},"arrangor":{"virksomhetNavn":"Tiltaksarrangør 1","organisasjonNavn":"Org Tiltaksarrangør 1"}},"fjernesDato":null,"innsokBegrunnelse":"begrunnelse deltaker 1"}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedBody

	}

}
