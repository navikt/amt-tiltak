package no.nav.amt.tiltak

import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableJwtTokenValidation
@Configuration
open class LocalApplicationConfig {

	@Bean
	open fun machineToMachineTokenClient(): MachineToMachineTokenClient {
		return MachineToMachineTokenClient { "MOCK_TOKEN" }
	}

	@Bean
	open fun corsConfigurer(): WebMvcConfigurer {
		return object : WebMvcConfigurer {
			override fun addCorsMappings(registry: CorsRegistry) {
				registry
					.addMapping("/**")
					.allowedOrigins("*")
					.allowedHeaders("*")
					.allowedMethods("*")
			}
		}
	}

}
