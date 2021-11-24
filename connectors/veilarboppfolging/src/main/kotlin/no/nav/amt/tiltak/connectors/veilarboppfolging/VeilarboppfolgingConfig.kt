package no.nav.amt.tiltak.connectors.veilarboppfolging

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider

@Configuration
open class VeilarboppfolgingConfig {

	@Bean
	open fun veilarboppfolgingConnector(
		@Value("poao-gcp-proxy.url") url: String,
		@Value("poao-gcp-proxy.scope") scope: String,
		scopedTokenProvider: ScopedTokenProvider
	): VeilarboppfolgingClient {
		return VeilarboppfolgingClient(url) { scopedTokenProvider.getToken(scope) }
	}
}
