package no.nav.amt.tiltak.clients.amt_altinn_acl

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

	test("hentTiltaksarrangorRoller - skal lage riktig request og parse respons") {
		val client = AmtAltinnAclClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		val org1 = "12345"
		val org2 = "56789"

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"roller": [
							{"organisasjonsnummer": "$org1", "roller": ["KOORDINATOR"]},
							{"organisasjonsnummer": "$org2", "roller": ["VEILEDER"]}
						]
					}
				""".trimIndent()
			)
		)

		val norskIdent = "237912"

		val rettigheter = client.hentTiltaksarrangorRoller(norskIdent)

		rettigheter shouldHaveSize 2
		rettigheter.any { it.organisasjonsnummer == org1 && it.roller.contains(TiltaksarrangorAnsattRolle.KOORDINATOR) }
		rettigheter.any { it.organisasjonsnummer == org2 && it.roller.contains(TiltaksarrangorAnsattRolle.VEILEDER) }

		val request = server.takeRequest()

		request.path shouldBe "/api/v1/rolle/tiltaksarrangor?norskIdent=$norskIdent"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

})

