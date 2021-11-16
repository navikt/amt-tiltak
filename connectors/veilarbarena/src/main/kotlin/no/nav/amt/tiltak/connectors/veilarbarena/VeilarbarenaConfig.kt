package no.nav.amt.tiltak.connectors.veilarbarena

import no.nav.amt.tiltak.core.port.VeilarbarenaConnector
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarbarenaConfig {

	@Bean
	open fun veilarbarenaConnector(
		@Value("poao-gcp-proxy.scope") scope: String,
		@Value("poao-gcp-proxy.url") url: String,
		scopedTokenProvider: ScopedTokenProvider
	): VeilarbarenaConnector {
		return VeilarbarenaConnectorImpl(
			url = "$url/proxy/veilarbarena",
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
