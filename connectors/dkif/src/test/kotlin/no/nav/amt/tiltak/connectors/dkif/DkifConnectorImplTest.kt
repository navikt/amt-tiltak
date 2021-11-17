package no.nav.amt.tiltak.connectors.dkif

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@WireMockTest
class DkifConnectorImplTest {

	@Test
	fun `hentBrukerKontaktinformasjon skal lage riktig request og parse respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = DkifConnectorImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		WireMock.givenThat(
			WireMock.get(urlEqualTo("/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
				.withHeader("Nav-Personidenter", equalTo("12345678900"))
				.willReturn(
					WireMock.aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
								  "kontaktinfo": {
								    "12345678900": {
								      "personident": "12345678900",
								      "kanVarsles": true,
								      "reservert": false,
								      "epostadresse": "testbruker@gmail.test",
								      "mobiltelefonnummer": "11111111"
								    }
								  }
								}
							""".trimIndent()
						)
				)

		)

		val kontaktinformasjon = client.hentBrukerKontaktinformasjon("12345678900")

		assertEquals("testbruker@gmail.test", kontaktinformasjon.epost)
		assertEquals("11111111", kontaktinformasjon.telefonnummer)
	}

	@Test
	fun `hentBrukerKontaktinformasjon skal returnere null på felter hvis kontaktinfo mangler`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = DkifConnectorImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		WireMock.givenThat(
			WireMock.get(urlEqualTo("/api/v1/personer/kontaktinformasjon?inkluderSikkerDigitalPost=false"))
				.willReturn(
					WireMock.aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
								  "kontaktinfo": {}
								}
							""".trimIndent()
						)
				)

		)

		val kontaktinformasjon = client.hentBrukerKontaktinformasjon("12345678900")

		assertNull(kontaktinformasjon.epost)
		assertNull(kontaktinformasjon.telefonnummer)
	}

}
