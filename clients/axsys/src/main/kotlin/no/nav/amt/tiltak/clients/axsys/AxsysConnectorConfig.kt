package no.nav.amt.tiltak.clients.axsys

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AxsysConnectorConfig {

	@Value("\${axsys.url}")
	lateinit var url: String

	@Value("\${axsys.scope}")
	lateinit var scope: String

	@Bean
	open fun axsysClient(scopedTokenProvider: ScopedTokenProvider): AxsysClient {
		val delegate = PlainAxsysClient(
			url = url,
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
		return CachedDelgatingAxsysClient(delegate)
	}

}
