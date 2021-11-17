package no.nav.amt.tiltak.connectors.dkif

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DkifConnectorConfig {

	@Bean
	open fun norgConnector(
		@Value("poao-gcp-proxy.scope") scope: String,
		@Value("poao-gcp-proxy.url") url: String,
		scopedTokenProvider: ScopedTokenProvider
	): DkifConnector {
		return DkifConnectorImpl(
			url = "$url/proxy/dkif",
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
