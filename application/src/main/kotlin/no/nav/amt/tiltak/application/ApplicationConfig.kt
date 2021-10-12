package no.nav.amt.tiltak.application

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.utils.AzureAdScopedTokenProviderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@Configuration
open class ApplicationConfig {

	@Bean
	open fun scopedTokenProvider(): ScopedTokenProvider {
		return AzureAdScopedTokenProviderBuilder.builder().withEnvironmentDefaults().build()
	}

}
