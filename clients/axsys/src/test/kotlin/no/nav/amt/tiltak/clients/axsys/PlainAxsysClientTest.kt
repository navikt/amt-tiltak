package no.nav.amt.tiltak.clients.axsys

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class AxsysClientTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentTilganger - med fodselsnummer - skal returnere korrekt parset response") {
		val brukerident = "AB12345"

		val client = PlainAxsysClient(
			baseUrl = serverUrl,
			proxyTokenProvider = { "PROXY_TOKEN" },
			axsysTokenProvider = { "AXSYS_TOKEN" },
		)

		System.setProperty("NAIS_APP_NAME", "amt-tiltak")

		server.enqueue(
			MockResponse().setBody(
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

		val tilganger = client.hentTilganger(brukerident)

		tilganger.first().enhetId shouldBe "0104"
		tilganger.first().navn shouldBe "NAV Moss"
		tilganger.first().temaer.containsAll(listOf("MOB", "OPA", "HJE")) shouldBe true

		val request = server.takeRequest()

		request.path shouldBe "/api/v2/tilgang/$brukerident?inkluderAlleEnheter=false"

		request.getHeader("Authorization") shouldBe "Bearer PROXY_TOKEN"
		request.getHeader("Nav-Consumer-Id") shouldBe "amt-tiltak"
		request.getHeader("Nav-Call-Id") shouldNotBe null
		request.getHeader("Downstream-Authorization") shouldBe "Bearer AXSYS_TOKEN"
	}

})
