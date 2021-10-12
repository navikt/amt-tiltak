package no.nav.amt.tiltak.tools.azure_ad

import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI

/**
 * Provides access tokens from Azure Ad through OAuth 2.0 credentials flow
 */
class AzureAdScopedTokenProvider(clientId: String, clientSecret: String, tokenEndpointUrl: String) : ScopedTokenProvider {

	private val log = LoggerFactory.getLogger(AzureAdScopedTokenProvider::class.java)

	private val clientAuth: ClientAuthentication
	private val tokenEndpoint: URI

	init {
		clientAuth = ClientSecretBasic(ClientID(clientId), Secret(clientSecret))
		tokenEndpoint = URI(tokenEndpointUrl)
	}

	override fun getToken(scope: String): String {
		val requestScope = Scope(scope)
		val grant = ClientCredentialsGrant()
		val request = TokenRequest(tokenEndpoint, clientAuth, grant, requestScope)
		var response: TokenResponse? = null

		try {
			response = TokenResponse.parse(request.toHTTPRequest().send())
		} catch (e: ParseException) {
			log.error("Failed to parse JWT token", e)
		} catch (e: IOException) {
			log.error("Failed to send token request", e)
		}

		checkNotNull(response) { "Failed to get token" }

		if (!response.indicatesSuccess()) {
			val tokenErrorResponse = response.toErrorResponse()
			log.error("Token request was not successful", tokenErrorResponse.toJSONObject().toString())
			throw RuntimeException("Failed to fetch service token for $scope")
		}

		val successResponse = response.toSuccessResponse()

		return successResponse.tokens.accessToken.value
	}

}
