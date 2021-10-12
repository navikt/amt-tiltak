package no.nav.amt.tiltak.tools.azure_ad

class AzureAdServiceTokenProvider(private val scopedTokenProvider: ScopedTokenProvider) : ServiceToServiceTokenProvider {

	override fun getServiceToken(serviceName: String, namespace: String, cluster: String): String {
		val serviceIdentifier = createServiceIdentifier(serviceName, namespace, cluster)
		val scope = createScope(serviceIdentifier)

		return scopedTokenProvider.getToken(scope)
	}

	companion object {
		private fun createScope(serviceIdentifier: String): String {
			return String.format("api://%s/.default", serviceIdentifier)
		}

		private fun createServiceIdentifier(serviceName: String, namespace: String, cluster: String): String {
			return String.format("%s.%s.%s", cluster, namespace, serviceName)
		}
	}

}
