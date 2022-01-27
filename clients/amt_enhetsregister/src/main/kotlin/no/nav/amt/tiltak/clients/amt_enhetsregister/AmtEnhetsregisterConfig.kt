package no.nav.amt.tiltak.clients.amt_enhetsregister

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AmtEnhetsregisterConfig {

	@Value("\${amt-enhetsregister.url}")
	lateinit var url: String

	@Value("\${amt-enhetsregister.scope}")
	lateinit var scope: String

	@Bean
	open fun enhetsregiserClient(scopedTokenProvider: ScopedTokenProvider): EnhetsregisterClient {
		return AmtEnhetsregisterClient(
			url = url,
			tokenProvider = { scopedTokenProvider.getToken(scope) },
		)
	}

}
