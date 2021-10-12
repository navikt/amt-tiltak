package no.nav.amt.tiltak.tools.token_provider

/**
 * Provides tokens for service to service authentication where each services requires a specific token
 */
interface ServiceToServiceTokenProvider {
	fun getServiceToken(serviceName: String, namespace: String, cluster: String): String
}
