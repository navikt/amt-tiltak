package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.integration.utils.TokenCreator

class MockMaskinportenHttpClient : MockHttpClient() {

	fun enqueueTokenResponse() {
		val token = TokenCreator.instance().createToken()

		enqueue(
			headers = mapOf("Content-Type" to "application/json"),
			body = """{ "token_type": "Bearer", "access_token": "$token", "expires": 3600 }"""
		)
	}

}
