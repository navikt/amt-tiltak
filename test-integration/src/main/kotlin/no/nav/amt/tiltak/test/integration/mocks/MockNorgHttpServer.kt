package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockNorgHttpServer : MockHttpServer(name = "MockNorgHttpServer") {

	private val baseUrl = "/norg2/api/v1/enhet"

	private val allResponses = mutableListOf<String>()

	fun reset() {
		resetHttpServer()
		allResponses.clear()
	}

	fun addDefaultData() {
		addNavEnhet("INTEGRATION_TEST_NAV_ENHET", "INTEGRATION_TEST_NAV_ENHET_NAVN")
	}

	fun addNavEnhet(enhetNr: String, navn: String) {
		val body = """
			{
				"navn": "$navn",
				"enhetNr": "$enhetNr"
			}
		""".trimIndent()

		val response = MockResponse().setResponseCode(200).setBody(body)
		addResponseHandler("$baseUrl/${enhetNr}", response)

		allResponses.add(body)
		addResponseHandler("$baseUrl/", MockResponse().setResponseCode(200).setBody(JsonUtils.toJsonString(allResponses)))
	}

}
