package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.LoggerFactory
import java.util.*

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
		ident: String,
		oid: UUID,
		claims: Map<String, Any> = mapOf(
			"NAVident" to ident,
			"oid" to oid.toString()
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
