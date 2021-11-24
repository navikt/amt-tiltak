package no.nav.amt.tiltak.tools.token_provider.azure_ad

import no.nav.amt.tiltak.tools.token_provider.CachedScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.OAuth2ScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.azure_ad.AzureAdEnvironmentVariables.AZURE_APP_CLIENT_ID
import no.nav.amt.tiltak.tools.token_provider.azure_ad.AzureAdEnvironmentVariables.AZURE_APP_CLIENT_SECRET
import no.nav.amt.tiltak.tools.token_provider.azure_ad.AzureAdEnvironmentVariables.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT

typealias ScopedTokenProviderCacheCreator = (provider: ScopedTokenProvider) -> ScopedTokenProvider

class AzureAdScopedTokenProviderBuilder private constructor() {
	private var clientId: String? = null
	private var clientSecret: String? = null
	private var tokenEndpointUrl: String? = null
	private var enableCache: Boolean = true
	private var cacheCreator: ScopedTokenProviderCacheCreator = { CachedScopedTokenProvider(it) }

	fun withEnvironmentDefaults(): AzureAdScopedTokenProviderBuilder {
        clientId = System.getenv(AZURE_APP_CLIENT_ID)
        clientSecret = System.getenv(AZURE_APP_CLIENT_SECRET)
        tokenEndpointUrl = System.getenv(AZURE_OPENID_CONFIG_TOKEN_ENDPOINT)
		return this
	}

	fun withEnableCache(enableCache: Boolean): AzureAdScopedTokenProviderBuilder {
		this.enableCache = enableCache
		return this
	}

	fun withCache(cacheCreator: ScopedTokenProviderCacheCreator): AzureAdScopedTokenProviderBuilder {
		this.cacheCreator = cacheCreator
		return this
	}

	fun withClientId(clientId: String): AzureAdScopedTokenProviderBuilder {
		this.clientId = clientId
		return this
	}

	fun withClientSecret(clientSecret: String): AzureAdScopedTokenProviderBuilder {
		this.clientSecret = clientSecret
		return this
	}

	fun withTokenEndpointUrl(tokenEndpointUrl: String): AzureAdScopedTokenProviderBuilder {
		this.tokenEndpointUrl = tokenEndpointUrl
		return this
	}

	fun build(): ScopedTokenProvider {
		val assertedClientId = checkNotNull(clientId) { "Client ID is required" }
		val assertedClientSecret = checkNotNull(clientSecret) { "Client secret is required" }
		val assertedTokenEndpointUrl = checkNotNull(tokenEndpointUrl) { "Token endpoint URL is required" }

		var scopedTokenProvider: ScopedTokenProvider = OAuth2ScopedTokenProvider(
			clientId = assertedClientId,
			clientSecret = assertedClientSecret,
			tokenEndpointUrl = assertedTokenEndpointUrl
		)

		if (enableCache) {
			scopedTokenProvider = cacheCreator.invoke(scopedTokenProvider)
		}

		return scopedTokenProvider
	}

	companion object {
		@JvmStatic
		fun builder(): AzureAdScopedTokenProviderBuilder {
			return AzureAdScopedTokenProviderBuilder()
		}
	}

}
