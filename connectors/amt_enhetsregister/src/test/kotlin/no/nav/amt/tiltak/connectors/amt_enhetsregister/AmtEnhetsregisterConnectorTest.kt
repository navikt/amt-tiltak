package no.nav.amt.tiltak.connectors.amt_enhetsregister

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@WireMockTest
class AmtEnhetsregisterConnectorTest {

	@Test
	fun `hentVirksomhet() skal lage riktig request og parse respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = AmtEnhetsregisterConnector(
			url = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		givenThat(
			get(urlEqualTo("/api/enhet/987654"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"navn": "Underenhet",
									"organisasjonsnummer": "1234",
									"overordnetEnhetNavn": "Overordnet enhet",
									"overordnetEnhetOrganisasjonsnummer": "5678"
								}
							""".trimIndent()
						)
				)

		)

		val virksomhet = client.hentVirksomhet("987654")

		assertEquals("Underenhet", virksomhet.navn)
		assertEquals("1234", virksomhet.organisasjonsnummer)
		assertEquals("Overordnet enhet", virksomhet.overordnetEnhetNavn)
		assertEquals("5678", virksomhet.overordnetEnhetOrganisasjonsnummer)
	}

}
