package no.nav.amt.tiltak.application

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.audit_log.log.AuditLoggerImpl
import no.nav.common.rest.filter.LogRequestFilter
import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.client.MachineToMachineTokenClient
import no.nav.common.utils.EnvironmentUtils
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@EnableJwtTokenValidation
@Configuration
open class ApplicationConfig {

	companion object {
		const val APPLICATION_NAME = "amt-tiltak"
	}

	@Bean
	open fun machineToMachineTokenClient(
		@Value("\${nais.env.azureAppClientId}") azureAdClientId: String,
		@Value("\${nais.env.azureOpenIdConfigTokenEndpoint}") azureTokenEndpoint: String,
		@Value("\${nais.env.azureAppJWK}") azureAdJWK: String,
	): MachineToMachineTokenClient {
		return AzureAdTokenClientBuilder.builder()
			.withClientId(azureAdClientId)
			.withTokenEndpointUrl(azureTokenEndpoint)
			.withPrivateJwk(azureAdJWK)
			.buildMachineToMachineTokenClient()
	}

	@Bean
	open fun auditLogger(): AuditLogger {
		return AuditLoggerImpl()
	}

	@Bean
	open fun poaoTilgangClient(
		@Value("\${poao-tilgang.url}") poaoTilgangUrl: String,
		@Value("\${poao-tilgang.scope}") poaoTilgangScope: String,
		machineToMachineTokenClient: MachineToMachineTokenClient
	): PoaoTilgangClient {
		return PoaoTilgangCachedClient(
			PoaoTilgangHttpClient(
				baseUrl = poaoTilgangUrl,
				tokenProvider = { machineToMachineTokenClient.createMachineToMachineToken(poaoTilgangScope) }
			)
		)
	}

	@Bean
	open fun logRequestFilterRegistrationBean(): FilterRegistrationBean<LogRequestFilter> {
		val registration = FilterRegistrationBean<LogRequestFilter>()
		registration.filter = LogRequestFilter(
			APPLICATION_NAME, EnvironmentUtils.isDevelopment().orElse(false)
		)
		registration.order = 1
		registration.addUrlPatterns("/*")
		return registration
	}

	@Bean
	@Profile("default")
	open fun unleashClient(
		@Value("\${app.env.unleashUrl}") unleashUrl: String,
		@Value("\${app.env.unleashApiToken}") unleashApiToken: String
	) : Unleash {
		val appName = "amt-tiltak"
		val config = UnleashConfig.builder()
			.appName(appName)
			.instanceId(appName)
			.unleashAPI(unleashUrl)
			.apiKey(unleashApiToken)
			.build()
		return DefaultUnleash(config)
	}

	@Bean
	open fun flywayCustomizer(): FlywayConfigurationCustomizer? {
		return FlywayConfigurationCustomizer { configuration: FluentConfiguration ->
			configuration.configuration(
				mapOf("flyway.postgresql.transactional.lock" to "false")
			)
		}
	}

}
