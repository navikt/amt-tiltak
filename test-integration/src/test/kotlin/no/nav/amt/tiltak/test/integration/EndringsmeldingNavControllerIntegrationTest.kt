package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EndringsmeldingNavControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding"
		)

		Assertions.assertEquals(401, response.code)
	}

	@Test
	fun `hentEndringsmeldinger() - skal returnere 403 hvis ikke tilgang til gjennomføring`() {
		poaoTilgangClient.addHentAdGrupperResponse()

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/endringsmelding?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken(ARRANGOR_ANSATT_1.personligIdent)}")
		)

		Assertions.assertEquals(403, response.code)
	}
}
