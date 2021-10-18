package no.nav.amt.tiltak.application

import no.nav.amt.tiltak.connectors.nom.client.NomConfig
import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.azure_ad.AzureAdScopedTokenProviderBuilder
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Profile("!local")
@EnableJwtTokenValidation
@Configuration
@Import(NomConfig::class)
open class ApplicationConfig {

	@Bean
	open fun scopedTokenProvider(): ScopedTokenProvider {
		return AzureAdScopedTokenProviderBuilder.builder().withEnvironmentDefaults().build()
	}

}
