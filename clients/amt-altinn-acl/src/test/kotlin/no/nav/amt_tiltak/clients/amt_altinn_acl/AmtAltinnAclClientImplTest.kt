package no.nav.amt_tiltak.clients.amt_altinn_acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class AmtAltinnAclClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentRettigheter() skal lage riktig request og parse respons") {
		val client = AmtAltinnAclClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"rettigheter": [
							{"id": "1233454", "organisasjonsnummer": "12345"},
							{"id": "9875442", "organisasjonsnummer": "56789"}
						]
					}
				""".trimIndent()
			)
		)

		val norskIdent = "237912"

		val rettighetIder = listOf("1233454", "9875442", "467328")

		val rettigheter = client.hentRettigheter(norskIdent, rettighetIder)

		rettigheter shouldHaveSize 2
		rettigheter.any { it.id == "1233454" && it.organisasjonsnummer == "12345" }
		rettigheter.any { it.id == "9875442" && it.organisasjonsnummer == "56789" }

		val request = server.takeRequest()

		val expectedRequestJson = """
			{"norskIdent":"$norskIdent","rettighetIder":["${rettighetIder[0]}","${rettighetIder[1]}","${rettighetIder[2]}"]}
		""".trimIndent()

		request.path shouldBe "/api/v1/rettighet/hent"
		request.method shouldBe "POST"
		request.body.readUtf8() shouldBe expectedRequestJson
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

})

