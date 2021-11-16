package no.nav.amt.tiltak.connectors.veilarbarena

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@WireMockTest
class VeilarbarenaConnectorImplTest {

	@Test
	fun `hentBrukerArenaStatus skal lage riktig request og parse respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = VeilarbarenaConnectorImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		givenThat(
			get(urlEqualTo("/veilarbarena/api/arena/status?fnr=987654"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
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

		val virksomhet = client.hentBrukerArenaStatus("987654")

		assertEquals("1234", virksomhet.oppfolgingsenhetId)
	}

}
