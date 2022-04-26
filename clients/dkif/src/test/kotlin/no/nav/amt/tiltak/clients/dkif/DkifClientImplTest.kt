package no.nav.amt.tiltak.clients.dkif

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class DkifClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentBrukerKontaktinformasjon skal lage riktig request og parse respons") {
		val client = DkifClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		System.setProperty("NAIS_APP_NAME", "amt-tiltak")

		server.enqueue(
			MockResponse().setBody(
				"""
						{
						  "personident": "12345678900",
						  "kanVarsles": true,
						  "reservert": false,
						  "epostadresse": "testbruker@gmail.test",
						  "mobiltelefonnummer": "11111111"
						}
					""".trimIndent()
			)
		)

		val kontaktinformasjon = client.hentBrukerKontaktinformasjon("12345678900")

		kontaktinformasjon.epost shouldBe "testbruker@gmail.test"
		kontaktinformasjon.telefonnummer shouldBe "11111111"

		val request = server.takeRequest()

		request.path shouldBe "/rest/v1/person?inkluderSikkerDigitalPost=false"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
		request.getHeader("Nav-Personident") shouldBe "12345678900"
		request.getHeader("Nav-Consumer-Id") shouldBe "amt-tiltak"
		request.getHeader("Nav-Call-Id") shouldNotBe null
	}

})
