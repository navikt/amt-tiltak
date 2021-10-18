package no.nav.amt.tiltak.connectors.nom.client

import no.nav.amt.tiltak.core.port.NomConnector
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Supplier

@EnableOAuth2Client(cacheEnabled=true)
@Configuration
open class NomConfig {

	@Bean("nomApiTokenSupplier")
	open fun tokenSupplier (
		clientConfigProps: ClientConfigurationProperties,
		accessTokenService: OAuth2AccessTokenService ) : Supplier<String> {
		val clientProperties: ClientProperties = clientConfigProps.registration["nom"]
			?: throw RuntimeException("could not find oauth2 client config for nom")

		return Supplier { accessTokenService.getAccessToken(clientProperties).accessToken }
	}

	@Bean
	open fun nomConnector(
		@Value("nom.api") nomApi: String,
		@Qualifier("nomApiTokenSupplier") tokenSupplier : Supplier<String>
	) : NomConnector {
		return NomClient(nomApiUrl = nomApi, tokenSupplier = tokenSupplier)
	}
}

