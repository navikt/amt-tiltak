package no.nav.amt.tiltak.clients.pdl

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PdlConnectorConfig {

	@Value("\${pdl.url}")
	lateinit var url: String

	@Value("\${pdl.scope}")
	lateinit var scope: String

	@Bean
	open fun pdlClient(scopedTokenProvider: ScopedTokenProvider): PdlClient {
		return PdlClientImpl(
			tokenProvider = { scopedTokenProvider.getToken(scope) },
			pdlUrl = url
		)
	}

}
