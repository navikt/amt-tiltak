package no.nav.amt.tiltak.connectors.norg

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NorgConnectorConfig {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var scope: String

	@Bean
	open fun norgConnector(scopedTokenProvider: ScopedTokenProvider): NorgConnector {
		return NorgConnectorImpl(
			url = "$url/proxy/norg2",
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
