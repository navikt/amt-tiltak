package no.nav.amt.tiltak.clients.veilarbarena

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class VeilarbarenaClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentBrukerOppfolgingsenhetId skal lage riktig request og parse respons") {
		val client = VeilarbarenaClientImpl(
			baseUrl = serverUrl,
			proxyTokenProvider = { "PROXY_TOKEN" },
			veilarbarenaTokenProvider = { "VEILARBARENA_TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
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

		val oppfolgingsenhetId = client.hentBrukerOppfolgingsenhetId("987654")

		oppfolgingsenhetId shouldBe "1234"

		val request = server.takeRequest()

		request.path shouldBe "/api/arena/status?fnr=987654"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer PROXY_TOKEN"
		request.getHeader("Downstream-Authorization") shouldBe "Bearer VEILARBARENA_TOKEN"
		request.getHeader("Nav-Consumer-Id") shouldBe "amt-tiltak"
	}

	test("hentBrukerOppfolgingsenhetId skal returnere null hvis veilarbarena returnerer 404") {
		val client = VeilarbarenaClientImpl(
			baseUrl = serverUrl,
			proxyTokenProvider = { "PROXY_TOKEN" },
			veilarbarenaTokenProvider = { "VEILARBARENA_TOKEN" },
		)

		server.enqueue(MockResponse().setResponseCode(404))

		client.hentBrukerOppfolgingsenhetId("987654") shouldBe null
	}

})
