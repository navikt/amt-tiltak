package no.nav.amt.tiltak.test.integration.mocks

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.slf4j.LoggerFactory
import java.util.*

open class MockOAuthServer {

	private val azureAdIssuer = "azuread"
	private val tokenXIssuer = "tokenx"

	val tilgangTilNavAnsattGroupId = "egen_ansatt_tilgang_id"
	val tiltakAnsvarligGroupId = "tiltak_ansvarlig_tilgang_id"
	val endringsmeldingGroupId = "endringsmelding_tilgang_id"



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
		adGroupIds: Array<String> = arrayOf(tiltakAnsvarligGroupId, endringsmeldingGroupId),
	): String {
		val claims = mapOf(
				"NAVident" to ident,
				"oid" to oid.toString(),
				"groups" to adGroupIds,
			)

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
