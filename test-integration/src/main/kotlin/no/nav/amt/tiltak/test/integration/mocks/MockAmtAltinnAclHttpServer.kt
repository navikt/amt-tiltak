package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_altinn_acl.AmtAltinnAclClientImpl
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockAmtAltinnAclHttpServer : MockHttpServer(name = "AmtAltinnAclHttpServer") {

	fun reset() {
		resetHttpServer()
	}

	fun addDefaultData() {
		addRoller(
			norskIdent = TestData.ARRANGOR_ANSATT_1.personligIdent,
			roller = createRollerForSingleOrg(ARRANGOR_1.organisasjonsnummer, listOf("KOORDINATOR"))
		)
	}

	fun addRoller(
		norskIdent: String,
		roller: AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response
	) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(roller))

		addResponseHandler("/api/v1/rolle/tiltaksarrangor?norskIdent=$norskIdent", response)
	}

	fun createRollerForSingleOrg(
		orgNr: String,
		roller: List<String>
	): AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response {
		return AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response(
			roller = listOf(
				AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response.TiltaksarrangorRoller(
					orgNr, roller
				)
			)
		)

	}

}
