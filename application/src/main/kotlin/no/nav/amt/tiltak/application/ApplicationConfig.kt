package no.nav.amt.tiltak.application

import no.nav.amt.tiltak.tools.token_provider.ScopedTokenProvider
import no.nav.amt.tiltak.tools.token_provider.azure_ad.AzureAdScopedTokenProviderBuilder
import no.nav.common.log.LogFilter
import no.nav.common.utils.EnvironmentUtils
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@EnableJwtTokenValidation
@Configuration
open class ApplicationConfig {

	@Bean
	open fun scopedTokenProvider(): ScopedTokenProvider {
		return AzureAdScopedTokenProviderBuilder.builder().withEnvironmentDefaults().build()
	}

	@Bean
	open fun logFilterRegistrationBean(): FilterRegistrationBean<LogFilter> {
		val registration = FilterRegistrationBean<LogFilter>()
		registration.filter = LogFilter(
			EnvironmentUtils.requireApplicationName(), EnvironmentUtils.isDevelopment().orElse(false)
		)
		registration.order = 1
		registration.addUrlPatterns("/*")
		return registration
	}

}
