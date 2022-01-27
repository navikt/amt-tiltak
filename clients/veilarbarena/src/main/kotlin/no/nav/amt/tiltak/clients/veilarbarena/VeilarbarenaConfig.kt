package no.nav.amt.tiltak.clients.veilarbarena

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarbarenaConfig {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var poaoGcpProxyScope: String

	@Value("\${veilarbarena.scope}")
	lateinit var veilarbarenaScope: String

	@Bean
	open fun veilarbarenaClient(scopedTokenProvider: ScopedTokenProvider): VeilarbarenaClient {
		return VeilarbarenaClientImpl(
			url = "$url/proxy/veilarbarena",
			proxyTokenProvider = { scopedTokenProvider.getToken(poaoGcpProxyScope) },
			veilarbarenaTokenProvider = { scopedTokenProvider.getToken(veilarbarenaScope) },
		)
	}

}
