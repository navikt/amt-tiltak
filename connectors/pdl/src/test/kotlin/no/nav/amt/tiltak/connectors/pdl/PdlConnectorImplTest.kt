package no.nav.amt.tiltak.connectors.pdl

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@WireMockTest
class PdlConnectorImplTest {

	@Test
	fun `hentBruker skal lage riktig request og parse pdl bruker`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val connector = PdlConnectorImpl(
			{ "TOKEN" },
			wmRuntimeInfo.httpBaseUrl
		)

		givenThat(
			post(urlEqualTo("/graphql"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
				.withHeader("Tema", equalTo("GEN"))
				.withRequestBody(
					equalToJson(
						"""
							{
								"query": "${PdlQueries.HentBruker.query.replace("\n", "\\n").replace("\t", "\\t")}",
								"variables": { "ident": "FNR" }
							}
						""".trimIndent()
					)
				)
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"errors": null,
									"data": {
										"hentPerson": {
											"navn": [
												{
													"fornavn": "Tester",
													"mellomnavn": "Test",
													"etternavn": "Testersen"
												}
											],
											"telefonnummer": [
												{
													"landskode": "+47",
													"nummer": "98765432",
													"prioritet": 2
												},
												{
													"landskode": "+47",
													"nummer": "12345678",
													"prioritet": 1
												}
											]
										}
									}
								}
							""".trimIndent()
						)
				)

		)

		val pdlBruker = connector.hentBruker("FNR")

		assertEquals("Tester", pdlBruker.fornavn)
		assertEquals("Test", pdlBruker.mellomnavn)
		assertEquals("Testersen", pdlBruker.etternavn)
		assertEquals("+47 12345678", pdlBruker.telefonnummer)
	}

	@Test
	fun `hentBruker skal kaste exception hvis data mangler`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val connector = PdlConnectorImpl(
			{ "TOKEN" },
			wmRuntimeInfo.httpBaseUrl
		)

		givenThat(
			post(urlEqualTo("/graphql"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"errors": [{"message": "error :("}],
									"data": null
								}
							""".trimIndent()
						)
				)
		)

		val exception = assertThrows<RuntimeException> {
			connector.hentBruker("FNR")
		}

		assertEquals("PDL respons inneholder ikke data", exception.message)
	}

	@Test
	fun `hentGjeldendePersonligIdent skal lage riktig request og parse response`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val connector = PdlConnectorImpl(
			{ "TOKEN" },
			wmRuntimeInfo.httpBaseUrl
		)

		givenThat(
			post(urlEqualTo("/graphql"))
				.withHeader("Authorization", equalTo("Bearer TOKEN"))
				.withHeader("Tema", equalTo("GEN"))
				.withRequestBody(
					equalToJson(
						"""
							{
								"query": "${PdlQueries.HentGjeldendeIdent.query.replace("\n", "\\n").replace("\t", "\\t")}",
								"variables": { "ident": "112233445566" }
							}
						""".trimIndent()
					)
				)
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"errors": null,
									"data": {
										"hentIdenter": {
										  "identer": [
											{
											  "ident": "12345678900"
											}
										  ]
										}
									  }
								}
							""".trimIndent()
						)
				)

		)

		val gjeldendeIdent = connector.hentGjeldendePersonligIdent("112233445566")
		assertEquals("12345678900", gjeldendeIdent)
	}

	@Test
	fun `hentGjeldendePersonligIdent skal kaste exception hvis data mangler`(wmRuntimeInfo: WireMockRuntimeInfo) {
		val connector = PdlConnectorImpl(
			{ "TOKEN" },
			wmRuntimeInfo.httpBaseUrl
		)

		givenThat(
			post(urlEqualTo("/graphql"))
				.willReturn(
					aResponse()
						.withStatus(200)
						.withBody(
							"""
								{
									"errors": [{"message": "error :("}],
									"data": null
								}
							""".trimIndent()
						)
				)
		)

		val exception = assertThrows<RuntimeException> {
			connector.hentGjeldendePersonligIdent("112233445566")
		}

		assertEquals("PDL respons inneholder ikke data", exception.message)
	}

}
