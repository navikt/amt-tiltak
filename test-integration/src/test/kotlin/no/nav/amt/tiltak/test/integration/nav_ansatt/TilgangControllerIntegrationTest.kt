package no.nav.amt.tiltak.test.integration.nav_ansatt

import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_3
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.inputs.TiltaksansvarligGjennomforingTilgangInput
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.test_utils.ControllerTestUtils.testNavAnsattAutentisering
import okhttp3.Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.*

class TilgangControllerIntegrationTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
		nomHttpServer.addDefaultData()
	}

	@Test
	internal fun `skal teste token autentisering`() {
		val requestBuilders = listOf(
			Request.Builder().post(emptyRequest()).url("${serverUrl()}/api/nav-ansatt/gjennomforing-tilgang?gjennomforingId=${UUID.randomUUID()}"),
			Request.Builder().patch(emptyRequest()).url("${serverUrl()}/api/nav-ansatt/gjennomforing-tilgang/stop?gjennomforingId=${UUID.randomUUID()}"),
		)
		testNavAnsattAutentisering(requestBuilders, client, oAuthServer)
	}

	@Test
	fun `giTilgangTilGjennomforing() - skal returnere 200 og gi tilgang til gjennomføring`() {
		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = UUID.randomUUID()
		)

		testDataRepository.insertGjennomforing(GJENNOMFORING_3)

		val response = sendRequest(
			method = "POST",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=${GJENNOMFORING_3.id}",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		Assertions.assertEquals(200, response.code)
	}


	@Test
	fun `stopTilgangTilGjennomforing() - skal returnere 200 og stoppe tilgang til gjennomføring`() {
		val token = oAuthServer.issueAzureAdToken(
			ident = NAV_ANSATT_1.navIdent,
			oid = UUID.randomUUID()
		)

		testDataRepository.insertTiltaksansvarligGjennomforingTilgang(
			TiltaksansvarligGjennomforingTilgangInput(
				id = UUID.randomUUID(),
				navAnsattId = NAV_ANSATT_1.id,
				gjennomforingId = GJENNOMFORING_1.id,
				gyldigTil = ZonedDateTime.now().plusDays(1),
				createdAt = ZonedDateTime.now()
			)
		)

		val response = sendRequest(
			method = "PATCH",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang/stop?gjennomforingId=${GJENNOMFORING_1.id}",
			headers = mapOf("Authorization" to "Bearer $token"),
			body = "".toJsonRequestBody()
		)

		Assertions.assertEquals(200, response.code)
	}


}
