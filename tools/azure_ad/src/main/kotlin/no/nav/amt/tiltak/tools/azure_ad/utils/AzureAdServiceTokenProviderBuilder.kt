package no.nav.amt.tiltak.tools.azure_ad.utils

import no.nav.amt.tiltak.tools.azure_ad.AzureAdScopedTokenProvider
import no.nav.amt.tiltak.tools.azure_ad.AzureAdServiceTokenProvider
import no.nav.amt.tiltak.tools.azure_ad.CachedScopedTokenProvider
import no.nav.amt.tiltak.tools.azure_ad.ScopedTokenProvider
import no.nav.amt.tiltak.tools.azure_ad.utils.AzureAdEnvironmentVariables.AZURE_APP_CLIENT_ID
import no.nav.amt.tiltak.tools.azure_ad.utils.AzureAdEnvironmentVariables.AZURE_APP_CLIENT_SECRET
import no.nav.amt.tiltak.tools.azure_ad.utils.AzureAdEnvironmentVariables.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT

class AzureAdServiceTokenProviderBuilder private constructor() {
	private var clientId: String? = null
	private var clientSecret: String? = null
	private var tokenEndpointUrl: String? = null
	private var enableCache: Boolean = true

	fun withEnvironmentDefaults(): AzureAdServiceTokenProviderBuilder {
        clientId = System.getenv(AZURE_APP_CLIENT_ID)
        clientSecret = System.getenv(AZURE_APP_CLIENT_SECRET)
        tokenEndpointUrl = System.getenv(AZURE_OPENID_CONFIG_TOKEN_ENDPOINT)
		return this
	}

	fun withCache(enableCache: Boolean): AzureAdServiceTokenProviderBuilder {
		this.enableCache = enableCache
		return this
	}

	fun withClientId(clientId: String): AzureAdServiceTokenProviderBuilder {
		this.clientId = clientId
		return this
	}

	fun withClientSecret(clientSecret: String): AzureAdServiceTokenProviderBuilder {
		this.clientSecret = clientSecret
		return this
	}

	fun withTokenEndpointUrl(tokenEndpointUrl: String): AzureAdServiceTokenProviderBuilder {
		this.tokenEndpointUrl = tokenEndpointUrl
		return this
	}

	fun build(): AzureAdServiceTokenProvider {
		val assertedClientId = checkNotNull(clientId) { "Client ID is required" }
		val assertedClientSecret = checkNotNull(clientSecret) { "Client secret is required" }
		val assertedTokenEndpointUrl = checkNotNull(tokenEndpointUrl) { "Token endpoint URL is required" }

		var scopedTokenProvider: ScopedTokenProvider = AzureAdScopedTokenProvider(
			clientId = assertedClientId,
			clientSecret = assertedClientSecret,
			tokenEndpointUrl = assertedTokenEndpointUrl
		)

		if (enableCache) {
			scopedTokenProvider = CachedScopedTokenProvider(scopedTokenProvider)
		}

		return AzureAdServiceTokenProvider(scopedTokenProvider)
	}

	companion object {
		@JvmStatic
		fun builder(): AzureAdServiceTokenProviderBuilder {
			return AzureAdServiceTokenProviderBuilder()
		}
	}

}
