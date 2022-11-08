package no.nav.amt.tiltak.test.integration.tiltaksarrangor

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testTiltaksarrangorAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AnsattControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}


	@Test
	internal fun `skal teste token autentisering`() {

		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/tiltaksarrangor/ansatt/meg"),
		)
		testTiltaksarrangorAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when authenticated`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}")
		)

		response.code shouldBe 200
	}

	@Test
	fun `getInnloggetAnsatt() should return 200 when ansatt is not previously stored`() {
		val ident = "012345678912"

		altinnAclHttpServer.addRoller(ident, altinnAclHttpServer.createRollerForSingleOrg(ARRANGOR_1.organisasjonsnummer, listOf("KOORDINATOR")))
		pdlHttpServer.addPdlBruker(ident, "Integrasjon", "Test")

		val response = sendRequest(
			method = "GET",
			url = "/api/tiltaksarrangor/ansatt/meg",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueTokenXToken(ident)}")
		)

		val expectedJson = """
			{"fornavn":"Integrasjon","etternavn":"Test","arrangorer":[{"id":"8a37bce6-3bc1-11ec-8d3d-0242ac130003","navn":"Tiltaksarrangør 1","organisasjonsnummer":"111111111","overordnetEnhetOrganisasjonsnummer":"911111111","overordnetEnhetNavn":"Org Tiltaksarrangør 1","roller":["KOORDINATOR"]}]}
		""".trimIndent()

		response.code shouldBe 200

		response.body?.string() shouldBe expectedJson

	}
}
