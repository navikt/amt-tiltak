package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class TiltaksansvarligGjennomforingTilgangControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		nomHttpClient.addDefaultData()
	}

	@Test
	fun `giTilgangTilGjennomforing() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=${UUID.randomUUID()}",
			body = "".toJsonRequestBody()
		)

		Assertions.assertEquals(401, response.code)
	}

	@Test
	fun `giTilgangTilGjennomforing() - skal returnere 200 og gi tilgang til gjennomføring`() {
		val token = oAuthServer.issueAzureAdToken(
			ident = TestData.NAV_ANSATT_1.navIdent,
			oid = UUID.randomUUID()
		)

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		Assertions.assertEquals(200, response.code)
	}

	@Test
	fun `stopTilgangTilGjennomforing() - skal returnere 401 hvis token mangler`() {
		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang/stop?gjennomforingId=${UUID.randomUUID()}",
			body = "".toJsonRequestBody()
		)

		Assertions.assertEquals(401, response.code)
	}


	//TODO
//	@Test
//	fun `stopTilgangTilGjennomforing() - skal returnere 200 og stoppe tilgang til gjennomføring`() {
//		val response = sendRequest(
//			method = "PATCH",
//			url = "/api/tiltaksansvarlig/gjennomforing-tilgang/stop?gjennomforingId=${GJENNOMFORING_1.id}",
//			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken(ARRANGOR_ANSATT_1.personligIdent)}"),
//			body = "".toJsonRequestBody()
//		)
//
//		Assertions.assertEquals(200, response.code)
//	}


}
