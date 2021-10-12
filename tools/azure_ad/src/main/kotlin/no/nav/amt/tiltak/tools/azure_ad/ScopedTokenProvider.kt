package no.nav.amt.tiltak.tools.azure_ad

/**
 * Provides tokens that uses OAuth 2.0 scope
 * See: https://oauth.net/2/scope
 */
interface ScopedTokenProvider {
	fun getToken(scope: String): String
}
