package no.nav.amt.tiltak.connectors.nom

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@EnableOAuth2Client(cacheEnabled=true)
@Configuration
open class NomConfig {

	@Bean
	open fun tokenSupplier (
		clientConfigProps: ClientConfigurationProperties,
		accessTokenService: OAuth2AccessTokenService ) : Supplier<String> {
		val clientProperties: ClientProperties = clientConfigProps.registration["nom"]
			?: throw RuntimeException("could not find oauth2 client config for nom")

		return Supplier { accessTokenService.getAccessToken(clientProperties).accessToken }
	}
}

