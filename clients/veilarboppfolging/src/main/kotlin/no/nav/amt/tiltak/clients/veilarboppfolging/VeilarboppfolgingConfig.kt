package no.nav.amt.tiltak.clients.veilarboppfolging

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class VeilarboppfolgingConfig {

	@Value("\${poao-gcp-proxy.url}")
	lateinit var url: String

	@Value("\${poao-gcp-proxy.scope}")
	lateinit var scope: String

	@Bean
	open fun veilarboppfolgingClient(scopedTokenProvider: ScopedTokenProvider): VeilarboppfolgingClient {
		return VeilarboppfolgingClientImpl("$url/proxy/veilarboppfolging", { scopedTokenProvider.getToken(scope) })
	}
}
