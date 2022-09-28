package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGrupper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class AutentiseringControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		resetMockServcersAndAddDefaultData()
	}

	@Test
	fun `meg() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg"
		)

		response.code shouldBe 401
	}

	@Test
	@Disabled
	fun `meg() - skal returnere 200 med korrekt response`() {
		poaoTilgangClient.addHentAdGrupperResponse(name = AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/autentisering/meg",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken(TestData.ARRANGOR_ANSATT_1.personligIdent)}"),
		)

		val expectedJson = """
			{"navIdent":"Z4321","navn":"Vashnir Veiledersen","tilganger":["FLATE"]}
		""".trimIndent()

		response.code shouldBe 200
		response.body?.string() shouldBe expectedJson

	}

}
