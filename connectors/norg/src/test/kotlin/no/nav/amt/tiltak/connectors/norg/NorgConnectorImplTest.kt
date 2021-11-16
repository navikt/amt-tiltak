package no.nav.amt.tiltak.connectors.norg

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@WireMockTest
class NorgConnectorImplTest {

	@Test
	fun `hentNavKontorNavn skal lage riktig request og parse respons`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val client = NorgConnectorImpl(
			url = wmRuntimeInfo.httpBaseUrl,
			tokenProvider = { "TOKEN" },
		)

		WireMock.givenThat(
			WireMock.get(WireMock.urlEqualTo("/api/v1/enhet/1234"))
				.withHeader("Authorization", WireMock.equalTo("Bearer TOKEN"))
				.willReturn(
					WireMock.aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
								  "enhetId": 900000042,
								  "navn": "NAV Testheim",
								  "enhetNr": "1234",
								  "antallRessurser": 330,
								  "status": "Aktiv",
								  "orgNivaa": "EN",
								  "type": "LOKAL",
								  "organisasjonsnummer": "12345645",
								  "underEtableringDato": "1970-01-01",
								  "aktiveringsdato": "1970-01-01",
								  "underAvviklingDato": null,
								  "nedleggelsesdato": null,
								  "oppgavebehandler": true,
								  "versjon": 40,
								  "sosialeTjenester": "Fritekst",
								  "kanalstrategi": null,
								  "orgNrTilKommunaltNavKontor": "123456789"
								}
							""".trimIndent()
						)
				)

		)

		val kontorNavn = client.hentNavKontorNavn("1234")

		assertEquals("NAV Testheim", kontorNavn)
	}

}
