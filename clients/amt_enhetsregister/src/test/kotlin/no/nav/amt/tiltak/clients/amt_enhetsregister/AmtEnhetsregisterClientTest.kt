package no.nav.amt.tiltak.clients.amt_enhetsregister

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class AmtEnhetsregisterClientTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentVirksomhet() skal lage riktig request og parse respons") {
		val client = AmtEnhetsregisterClient(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
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

		val virksomhet = client.hentVirksomhet("987654")

		virksomhet.navn shouldBe "Underenhet"
		virksomhet.organisasjonsnummer shouldBe "1234"
		virksomhet.overordnetEnhetNavn shouldBe "Overordnet enhet"
		virksomhet.overordnetEnhetOrganisasjonsnummer shouldBe "5678"

		val request = server.takeRequest()

		request.path shouldBe "/api/enhet/987654"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

	test("hentVirksomhet() skal returnere default virksomhet hvis amt-enhetsregister returnerer 404") {
		val client = AmtEnhetsregisterClient(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		server.enqueue(MockResponse().setResponseCode(404))

		val virksomhet = client.hentVirksomhet("987654")

		virksomhet.navn shouldBe "Ukjent virksomhet"
		virksomhet.organisasjonsnummer shouldBe "987654"
		virksomhet.overordnetEnhetNavn shouldBe "Ukjent virksomhet"
		virksomhet.overordnetEnhetOrganisasjonsnummer shouldBe "999999999"

		val request = server.takeRequest()

		request.path shouldBe "/api/enhet/987654"
	}

})
