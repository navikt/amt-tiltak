package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ApiTestUtils.testNavAnsattAutentisering
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AutentiseringAPIIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServersAndAddDefaultData()
	}


	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().get().url("${serverUrl()}/api/nav-ansatt/autentisering/meg"),
		)
		testNavAnsattAutentisering(requestBuilders, client, mockOAuthServer)
	}

	@Test
	fun `meg() - skal returnere 200 med korrekt response`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = TestData.NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.tilgangTilNavAnsattGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.endringsmeldingGroupId,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg",
			headers = mapOf("Authorization" to "Bearer $token"),
		)

		val expectedJson = """
			{"navIdent":"Z4321","navn":"Vashnir Veiledersen","tilganger":["EGNE_ANSATTE","FLATE","ENDRINGSMELDING"]}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson

	}

	@Test
	fun `meg() - skal returnere 200 med korrekt response uten tilganger`() {
		val oid = UUID.randomUUID()

		val token = mockOAuthServer.issueAzureAdToken(
			ident = TestData.NAV_ANSATT_1.navIdent,
			oid = oid,
			adGroupIds = arrayOf("Ukjent AD-gruppe"),
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg",
			headers = mapOf("Authorization" to "Bearer $token"),
		)

		val expectedJson = """
			{"navIdent":"Z4321","navn":"Vashnir Veiledersen","tilganger":[]}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson

	}

	@Test
	fun `meg() - ny ansatt med tilganger - skal opprette ansatt og returnere riktig respons`() {
		val oid = UUID.randomUUID()
		val navAnsatt = NavAnsatt(
			id = UUID.randomUUID(),
			navIdent = "Z42777",
			navn = "Ny Nav Ansatt",
			epost = "ansatt@nav.no",
			telefonnummer = "77742777",
		)

		mockAmtPersonHttpServer.addAnsattResponse(navAnsatt)

		val token = mockOAuthServer.issueAzureAdToken(
			ident = navAnsatt.navIdent,
			oid = oid,
			adGroupIds = arrayOf(
				mockOAuthServer.tilgangTilNavAnsattGroupId,
				mockOAuthServer.tiltakAnsvarligGroupId,
				mockOAuthServer.endringsmeldingGroupId,
			)
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg",
			headers = mapOf("Authorization" to "Bearer $token"),
		)

		val expectedJson = """
			{"navIdent":"Z42777","navn":"Ny Nav Ansatt","tilganger":["EGNE_ANSATTE","FLATE","ENDRINGSMELDING"]}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson

	}

}
