package no.nav.amt.tiltak.test.integration.mocks

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.LoggerFactory

open class MockOAuthServer3 {

	private val azureAdIssuer = "azuread"

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

	fun getDiscoveryUrl(issuer: String = azureAdIssuer): String {
		return server.wellKnownUrl(issuer).toString()
	}

	fun shutdown() {
		server.shutdown()
	}

	fun issueAzureAdToken(
		subject: String = "test",
		audience: String = "test-aud",
		claims: Map<String, Any> = emptyMap()
	): String {
		return server.issueToken(azureAdIssuer, subject, audience, claims).serialize()
	}

	fun issueAzureAdM2MToken(
		subject: String = "test",
		audience: String = "test-aud",
		claims: Map<String, Any> = emptyMap()
	): String {
		val claimsWithRoles = claims.toMutableMap()
//		claimsWithRoles["roles"] = arrayOf(AuthService.ACCESS_AS_APPLICATION_ROLE)

		return server.issueToken(azureAdIssuer, subject, audience, claimsWithRoles).serialize()
	}

}
