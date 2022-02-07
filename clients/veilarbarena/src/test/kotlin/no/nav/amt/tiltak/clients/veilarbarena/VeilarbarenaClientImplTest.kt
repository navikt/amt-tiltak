package no.nav.amt.tiltak.clients.veilarbarena

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@WireMockTest
class VeilarbarenaClientImplTest {

	@Test
	fun `hentBrukerOppfolgingsenhetId skal lage riktig request og parse respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = VeilarbarenaClientImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			proxyTokenProvider = { "PROXY_TOKEN" },
			veilarbarenaTokenProvider = { "VEILARBARENA_TOKEN" },
		)

		givenThat(
			get(urlEqualTo("/api/arena/status?fnr=987654"))
				.withHeader("Downstream-Authorization", equalTo("Bearer VEILARBARENA_TOKEN"))
				.withHeader("Authorization", equalTo("Bearer PROXY_TOKEN"))
				.withHeader("Nav-Consumer-Id", equalTo("amt-tiltak"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"formidlingsgruppe": "ARBS",
									"kvalifiseringsgruppe": "BFORM",
									"rettighetsgruppe": "DAGP",
									"iservFraDato": "2021-11-16T10:09:03",
									"oppfolgingsenhet": "1234"
								}
							""".trimIndent()
						)
				)

		)

		val oppfolgingsenhetId = client.hentBrukerOppfolgingsenhetId("987654")

		assertEquals("1234", oppfolgingsenhetId)
	}

	@Test
	fun `hentBrukerOppfolgingsenhetId skal returnere null hvis veilarbarena returnerer 404`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = VeilarbarenaClientImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			proxyTokenProvider = { "PROXY_TOKEN" },
			veilarbarenaTokenProvider = { "VEILARBARENA_TOKEN" },
		)

		givenThat(
			get(urlEqualTo("/api/arena/status?fnr=987654"))
				.willReturn(aResponse().withStatus(404))
		)

		assertNull(client.hentBrukerOppfolgingsenhetId("987654"))
	}

}
