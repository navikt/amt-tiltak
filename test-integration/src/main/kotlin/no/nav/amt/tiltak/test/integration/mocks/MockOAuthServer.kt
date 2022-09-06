package no.nav.amt.tiltak.test.integration.mocks

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.LoggerFactory

open class MockOAuthServer3 {

	private val azureAdIssuer = "azuread"
	private val tokenXIssuer = "tokenx"


	private val log = LoggerFactory.getLogger(javaClass)

	companion object {
		private val server = MockOAuth2Server()
	}

	fun start() {
		try {
			server.start()
		} catch (e: IllegalArgumentException) {
			log.info("${javaClass.simpleName} is already started")
		}
	}

	fun getDiscoveryUrl(issuer: String): String {
		return server.wellKnownUrl(issuer).toString()
	}

	fun shutdown() {
		server.shutdown()
	}

	fun issueAzureAdToken(
		subject: String = "test",
		audience: String = "test-aud",
		claims: Map<String, Any> = mapOf(
			"NAVident" to "Z123",
			"oid" to "e2bae1e5-94c8-4ef6-9d7a-4d2e04b5ae1c"
		)
	): String {
		return server.issueToken(azureAdIssuer, subject, audience, claims).serialize()
	}

	fun issueTokenXToken(
		ansattPersonIdent: String,
		subject: String = "test",
		audience: String = "test-aud",
	): String {

		val claims: Map<String, Any> = mapOf(
			"pid" to ansattPersonIdent
		)

		return server.issueToken(tokenXIssuer, subject, audience, claims).serialize()
	}


}
