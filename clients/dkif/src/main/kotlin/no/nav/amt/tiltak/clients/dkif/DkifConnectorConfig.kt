package no.nav.amt.tiltak.clients.dkif

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DkifConnectorConfig {

	@Value("\${digdir-krr-proxy.url}")
	lateinit var url: String

	@Value("\${digdir-krr-proxy.scope}")
	lateinit var scope: String

	@Bean
	open fun dkifClient(scopedTokenProvider: ScopedTokenProvider): DkifClient {
		return DkifClientImpl(
			url = url,
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
