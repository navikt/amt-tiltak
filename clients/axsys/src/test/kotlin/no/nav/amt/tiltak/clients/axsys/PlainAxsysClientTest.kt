package no.nav.amt.tiltak.clients.axsys

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@WireMockTest
internal class PlainAxsysClientTest {

	private val brukerident = "AB12345"

	@Test
	fun `hentTilganger - med fodselsnummer - skal returnere korrekt parset respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = PlainAxsysClient(
			baseUrl = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		System.setProperty("NAIS_APP_NAME", "amt-tiltak")

		givenThat(
			get(urlEqualTo("/api/v2/tilgang/$brukerident?inkluderAlleEnheter=false"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
				.withHeader("Nav-Consumer-Id", equalTo("amt-tiltak"))
				.withHeader("Nav-Call-Id", matching("[0-9a-fA-F]{32}"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
								  "enheter": [
									{
									  "enhetId": "0104",
									  "temaer": [
										"MOB",
										"OPA",
										"HJE"
									  ],
									  "navn": "NAV Moss"
									}
								  ]
								}
							""".trimIndent()
						)
				)

		)

		val tilganger = client.hentTilganger(brukerident)

		assertEquals("0104", tilganger.first().enhetId)
		assertTrue(tilganger.first().temaer.containsAll(listOf("MOB", "OPA", "HJE")))
		assertEquals("NAV Moss", tilganger.first().navn)
	}

}
