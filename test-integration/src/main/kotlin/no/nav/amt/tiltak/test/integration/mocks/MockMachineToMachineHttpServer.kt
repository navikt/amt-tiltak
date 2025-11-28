package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class MockMachineToMachineHttpServer : MockHttpServer(name = "MockMachineToMachineHttpServer") {
	init {
		addMockTokenToResponse()
	}

	private fun addMockTokenToResponse() {
		val predicate = { req: RecordedRequest ->
			req.path == TOKEN_PATH
		}

		val response = MockResponse()
			.setResponseCode(200)
			.setBody(mockTokenJson)
			.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

		addResponseHandler(predicate, response)
	}

	companion object {
		const val TOKEN_PATH = "/token"

		// Inneholder {"sub": "1234567890","name": "John Doe","iat": 1516239022}, iat = 17. januar 2018, 04:30:22 UTC
		private const val MOCK_ACCESS_TOKEN =
			"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

		private const val MOCK_REFRESH_TOKEN = "ccf818e6-9114-45cf-819f-c030f153cf13"

		private val mockTokenJson =
			"""
			{
			  "token_type" : "Bearer",
			  "id_token" : "$MOCK_ACCESS_TOKEN",
			  "access_token" : "$MOCK_ACCESS_TOKEN",
			  "refresh_token" : "$MOCK_REFRESH_TOKEN",
			  "expires_in" : 31535999,
			  "scope" : "openid somescope"
			}
			""".trimIndent()
	}
}
