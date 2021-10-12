package no.nav.amt.tiltak.tools.token_provider

import com.github.tomakehurst.wiremock.client.BasicCredentials
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

@WireMockTest
class OAuth2ScopedTokenProviderTest {

	@Test
	fun `should create correct token request`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val clientId = "CLIENT_ID"
		val clientSecret = "CLIENT_SECRET"
		val scope = "SCOPE"

		val accessToken: String = PlainJWT(
			JWTClaimsSet.Builder().expirationTime(Date(System.currentTimeMillis() + 10000)).build()
		).serialize()

		givenThat(
			post(anyUrl())
				.willReturn(
					aResponse()
						.withStatus(200)
						.withHeader("Authorization", "Q0xJRU5UX0lEOkNMSUVOVF9TRUNSRVQ=")
						.withHeader("Content-Type", "application/json")
						.withBody(
							"""
								{ "token_type": "bearer", "access_token": "$accessToken" }
							""".trimIndent()
						)
				)
		)

		val tokenProvider = OAuth2ScopedTokenProvider(
			clientId,
			clientSecret,
			"${wmRuntimeInfo.httpBaseUrl}/oauth2/v2.0/token"
		)

		val token = tokenProvider.getToken(scope)

		assertEquals(accessToken, token)

		verify(
			postRequestedFor(urlEqualTo("/oauth2/v2.0/token"))
				.withRequestBody(containing("grant_type=client_credentials"))
				.withRequestBody(containing("scope=$scope"))
				.withBasicAuth(BasicCredentials(clientId, clientSecret))
		)
	}

}
