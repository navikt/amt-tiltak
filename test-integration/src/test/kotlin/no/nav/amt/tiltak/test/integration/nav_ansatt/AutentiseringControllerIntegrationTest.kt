package no.nav.amt.tiltak.test.integration.nav_ansatt

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testNavAnsattAutentisering
import no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.ad_gruppe.AdGrupper
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AutentiseringControllerIntegrationTest : IntegrationTestBase() {

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
		testNavAnsattAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	fun `meg() - skal returnere 200 med korrekt response`() {
		val oid = UUID.randomUUID()

		poaoTilgangClient.addHentAdGrupperResponse(
			navAnsattAzureId = oid,
			name = AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE
		)

		val token = oAuthServer.issueAzureAdToken(
			ident = TestData.NAV_ANSATT_1.navIdent,
			oid = oid
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg",
			headers = mapOf("Authorization" to "Bearer $token"),
		)

		val expectedJson = """
			{"navIdent":"Z4321","navn":"Vashnir Veiledersen","tilganger":["FLATE"]}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson

	}

}
