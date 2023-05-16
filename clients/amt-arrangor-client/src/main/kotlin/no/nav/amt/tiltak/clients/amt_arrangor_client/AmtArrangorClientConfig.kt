package no.nav.amt.tiltak.clients.amt_arrangor_client

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableOAuth2Client(cacheEnabled = true)
open class AmtArrangorClientConfig(
	private val clientConfigurationProperties: ClientConfigurationProperties,
	private val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

	@Value("\${amt-arrangor.url}")
	lateinit var url: String

	@Value("\${amt-arrangor.scope}")
	lateinit var scope: String


	@Bean
	open fun amtArrangorClient(): AmtArrangorClient {
		return AmtArrangorClient(
			baseUrl = url,
			httpClient = tokenXClient()
		)
	}

	private fun tokenXClient(): OkHttpClient {
		val registrationName = "amt-tiltak-tokenx"
		val clientProperties = clientConfigurationProperties.registration[registrationName]
			?: throw RuntimeException("Fant ikke config for $registrationName")
		return OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(15, TimeUnit.SECONDS)
			.followRedirects(false)
			.addInterceptor(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
			.build()
	}

	private fun bearerTokenInterceptor(
		clientProperties: ClientProperties,
		oAuth2AccessTokenService: OAuth2AccessTokenService
	): Interceptor {
		return Interceptor { chain: Interceptor.Chain ->
			val accessTokenResponse = oAuth2AccessTokenService.getAccessToken(clientProperties)
			val request = chain.request()
			val requestWithToken = request.newBuilder()
				.addHeader("Authorization", "Bearer ${accessTokenResponse.accessToken}")
				.build()
			chain.proceed(requestWithToken)
		}
	}


}
