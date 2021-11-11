package no.nav.amt.tiltak.connectors.amt_enhetsregister

import no.nav.amt.tiltak.core.port.EnhetsregisterConnector
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AmtEnhetsregisterConfig {

	@Bean
	open fun enhetsregisterConnector(
		@Value("amt-enhetsregister.scope") scope: String,
		@Value("amt-enhetsregister.url") url: String,
		scopedTokenProvider: ScopedTokenProvider
	): EnhetsregisterConnector {
		return AmtEnhetsregisterConnector(
			url = url,
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
