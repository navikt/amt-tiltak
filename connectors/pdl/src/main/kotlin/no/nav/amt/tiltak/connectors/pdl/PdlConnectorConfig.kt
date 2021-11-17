package no.nav.amt.tiltak.connectors.pdl

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class PdlConnectorConfig {

	@Bean
	open fun pdlConnector(
		@Value("pdl.scope") pdlScope: String,
		@Value("pdl.url") pdlUrl: String,
		scopedTokenProvider: ScopedTokenProvider
	): PdlConnector {
		return PdlConnectorImpl(
			tokenProvider = { scopedTokenProvider.getToken(pdlScope) },
			pdlUrl = pdlUrl
		)
	}

}
