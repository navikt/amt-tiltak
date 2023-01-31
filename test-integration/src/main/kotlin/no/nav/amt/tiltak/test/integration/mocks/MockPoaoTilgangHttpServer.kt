package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory

class MockPoaoTilgangHttpServer : MockHttpServer(name = "MockPoaoTilgangHttpServer") {

	fun reset() {
		resetHttpServer()
	}

	fun addErSkjermetResponse(data: Map<String, Boolean>) {
		val url = "/api/v1/skjermet-person"

		val predicate = { req: RecordedRequest ->
			val body = req.body.readUtf8()

			req.path == url
				&& req.method == "POST"
				&& data.keys.map { body.contains(it) }.all { true }
		}

		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(data))

		addResponseHandler(predicate, response)
	}

}
