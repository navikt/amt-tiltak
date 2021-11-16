package no.nav.amt.tiltak.connectors.norg

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NorgConnectorConfig {

	@Bean
	open fun norgConnector(
		@Value("poao-gcp-proxy.scope") scope: String,
		@Value("poao-gcp-proxy.url") url: String,
		scopedTokenProvider: ScopedTokenProvider
	): NorgConnector {
		return NorgConnectorImpl(
			url = "$url/proxy/norg",
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
