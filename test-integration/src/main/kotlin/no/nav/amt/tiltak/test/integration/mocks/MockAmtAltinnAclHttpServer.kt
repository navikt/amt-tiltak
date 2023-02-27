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
			orgNr = ARRANGOR_1.organisasjonsnummer,
			roller = listOf("KOORDINATOR")
		)

		addRoller(
			norskIdent = TestData.ARRANGOR_ANSATT_2.personligIdent,
			orgNr = ARRANGOR_1.organisasjonsnummer,
			roller = listOf("VEILEDER")
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

	fun addRoller(norskIdent: String, orgNr: String, roller: List<String>) {
		addRoller(norskIdent, createRolleResponse(mapOf(orgNr to roller)))
	}

	fun addRoller(norskIdent: String, rollerPerOrg: Map<String, List<String>>) {
		addRoller(norskIdent, createRolleResponse(rollerPerOrg))
	}

	private fun createRolleResponse(
		rollerPerOrg: Map<String, List<String>>
	): AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response {
		return AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response(
			roller = rollerPerOrg.map {
				AmtAltinnAclClientImpl.HentTiltaksarrangorRoller.Response.TiltaksarrangorRoller(
					it.key, it.value
				)
			})
	}
}
