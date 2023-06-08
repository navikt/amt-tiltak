package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockAmtArrangorServer : MockHttpServer("amt-arrangor-server") {

	fun reset() {
		resetHttpServer()
	}

	fun addAnsattResponse(
		ansattDto: AmtArrangorClient.AnsattDto
	) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(ansattDto))

		addResponseHandler("/api/service/ansatt", response)
	}

	fun addArrangorResponse(
		arrangor: AmtArrangorClient.ArrangorMedOverordnetArrangor
	) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(arrangor))

		addResponseHandler("/api/service/arrangor/organisasjonsnummer/${arrangor.organisasjonsnummer}", response)
	}
}
