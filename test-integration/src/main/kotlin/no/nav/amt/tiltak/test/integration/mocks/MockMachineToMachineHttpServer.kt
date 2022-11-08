package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockMachineToMachineHttpServer : MockHttpServer(name = "MockMachineToMachineHttpServer") {

	companion object {
		const val tokenPath = "/token"
	}

	init {
		mockToken()
	}

	private fun mockToken() {
		val predicate = { req: RecordedRequest ->
			req.path == tokenPath
		}

		val body = """
			{
			  "token_type" : "Bearer",
			  "id_token" : "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
			  "access_token" : "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
			  "refresh_token" : "ccf818e6-9114-45cf-819f-c030f153cf13",
			  "expires_in" : 31535999,
			  "scope" : "openid somescope"
			}
		""".trimIndent()

		val response = MockResponse().setResponseCode(200).setBody(body).setHeader("content-type", "application/json")

		addResponseHandler(predicate, response)
	}
}
