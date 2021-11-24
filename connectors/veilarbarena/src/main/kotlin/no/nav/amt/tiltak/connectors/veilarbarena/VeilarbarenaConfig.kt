package no.nav.amt.tiltak.connectors.veilarbarena

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarbarenaConfig {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var scope: String

	@Bean
	open fun veilarbarenaConnector(scopedTokenProvider: ScopedTokenProvider): VeilarbarenaConnector {
		return VeilarbarenaConnectorImpl(
			url = "$url/proxy/veilarbarena",
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
