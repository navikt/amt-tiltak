package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.mocks.MockPdlBruker
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AnsattControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {

		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/ansatt/meg/roller"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	internal fun `getRoller - Har ikke roller - Returnerer tomt set`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			norskIdent = ident,
			orgNr = ARRANGOR_1.organisasjonsnummer,
			roller = listOf()
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		response.body?.string() shouldBe "[]"
	}

	@Test
	internal fun `getRoller - Har flere koordinator roller - Returnerer KOORDINATOR`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			ident,
			mapOf(
				ARRANGOR_1.organisasjonsnummer to listOf("KOORDINATOR"),
				ARRANGOR_2.organisasjonsnummer to listOf("KOORDINATOR")
			)
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		val responseBody = JsonUtils.fromJsonString<Set<String>>(response.body?.string()!!)

		responseBody shouldBe setOf("KOORDINATOR")
	}

	@Test
	internal fun `getRoller - Har koordinator og veileder roller - Returnerer KOORDINATOR, VEILEDER`() {
		val ident = UUID.randomUUID().toString()

		mockAmtAltinnAclHttpServer.addRoller(
			ident,
			mapOf(
				ARRANGOR_1.organisasjonsnummer to listOf("KOORDINATOR"),
				ARRANGOR_2.organisasjonsnummer to listOf("VEILEDER")
			)
		)
		mockPdlHttpServer.mockHentBruker(ident, MockPdlBruker("Integrasjon", "Test"))

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg/roller",
			headers = mapOf("Authorization" to "Bearer ${mockOAuthServer.issueTokenXToken(ident)}")
		)

		val responseBody = JsonUtils.fromJsonString<Set<String>>(response.body?.string()!!)

		responseBody shouldBe setOf("KOORDINATOR", "VEILEDER")
	}

}
