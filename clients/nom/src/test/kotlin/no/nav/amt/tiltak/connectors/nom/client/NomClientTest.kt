package no.nav.amt.tiltak.connectors.nom.client

import io.kotest.assertions.fail
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class NomGraphqlClientTest : StringSpec({

	val token = "DUMMYTOKEN"

	lateinit var server: MockWebServer
	lateinit var client: NomClientImpl
	isolationMode = IsolationMode.InstancePerTest

	beforeTest {
		server = MockWebServer()

		client = NomClientImpl(server.url("").toString(), { token })
	}

	"hentVeileder - veileder finnes - returnerer veileder" {
		val veilederRespons = """
			{
			  "data": {
				"ressurser": [
				  {
					"ressurs": {
					  "navIdent": "H156147",
					  "visningsNavn": "Alias",
					  "fornavn": "Blaut",
					  "etternavn": "Slappfisk",
					  "epost": "blaut.slappfisk@nav.no",
					  "telefon": [{ "type": "NAV_TJENESTE_TELEFON", "nummer": "12345678" }]
					},
					"code": "OK"
				  }
				]
			  }
			}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentVeileder("H156147") ?: fail("Veileder er null")

		veileder.navIdent shouldBe "H156147"
		veileder.visningNavn shouldBe "Alias"
		veileder.fornavn shouldBe "Blaut"
		veileder.etternavn shouldBe "Slappfisk"
		veileder.epost shouldBe "blaut.slappfisk@nav.no"
		veileder.telefonnummer shouldBe "12345678"
	}

	"hentVeileder - skal ikke hente privat telefonnummer" {
		val veilederRespons = """
			{
			  "data": {
				"ressurser": [
				  {
					"ressurs": {
					  "navIdent": "H156147",
					  "visningsNavn": "Alias",
					  "fornavn": "Blaut",
					  "etternavn": "Slappfisk",
					  "epost": "blaut.slappfisk@nav.no",
					  "telefon": [{ "type": "PRIVAT_TELEFON", "nummer": "12345678" }]
					},
					"code": "OK"
				  }
				]
			  }
		}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentVeileder("H156147") ?: fail("Veileder er null")

		veileder.telefonnummer shouldBe null
	}

	"hentVeileder - veileder finnes ikke - returnerer null" {
		val veilederRespons = """
			{
				"data": {
					"ressurser": [
					  {
						"code": "NOT_FOUND",
						"ressurs": null
					  }
					]
				}
			}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentVeileder("test")
		veileder shouldBe null
	}

	"hentVeileder - token legges på - token mottas på server" {
		val veilederRespons = """
			{
				"data": {
					"ressurser": [
					  {
						"code": "NOT_FOUND",
						"ressurs": null
					  }
					]
				}
			}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		client.hentVeileder("test")

		val recordedRequest = server.takeRequest()

		recordedRequest.getHeader("Authorization") shouldBe "Bearer $token"
	}
})



