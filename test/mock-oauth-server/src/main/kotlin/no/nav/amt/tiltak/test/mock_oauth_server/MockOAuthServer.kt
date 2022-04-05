package no.nav.amt.tiltak.test.mock_oauth_server

import no.nav.amt.tiltak.common.auth.Issuer
import no.nav.security.mock.oauth2.MockOAuth2Server

open class MockOAuthServer {
	private val server = MockOAuth2Server()

	init {
		server.start()
		System.setProperty("MOCK_AZURE_AD_DISCOVERY_URL", server.wellKnownUrl(Issuer.AZURE_AD).toString())
		System.setProperty("MOCK_TOKEN_X_DISCOVERY_URL", server.wellKnownUrl(Issuer.TOKEN_X).toString())
	}

	fun shutdownMockServer() {
		server.shutdown()
	}

	fun azureAdToken(
		subject: String = "test",
		audience: String = "test",
		claims: Map<String, Any> = emptyMap()
	): String {
		return server.issueToken(Issuer.AZURE_AD, subject, audience, claims).serialize()
	}

	fun tokenXToken(
		subject: String = "test",
		audience: String = "test",
		claims: Map<String, Any> = emptyMap()
	): String {
		return server.issueToken(Issuer.TOKEN_X, subject, audience, claims).serialize()
	}
}
