package no.nav.amt.tiltak.clients.nom

import io.kotest.assertions.fail
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class NomGraphqlClientTest : StringSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	val token = "DUMMYTOKEN"

	lateinit var client: NomClientImpl
	isolationMode = IsolationMode.InstancePerTest

	beforeTest {
		client = NomClientImpl(serverUrl, { token })
	}

	"hentVeileder - veileder finnes - returnerer veileder med alias" {
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

		val veileder = client.hentNavAnsatt("H156147") ?: fail("Veileder er null")

		veileder.navIdent shouldBe "H156147"
		veileder.navn shouldBe "Alias"
		veileder.epost shouldBe "blaut.slappfisk@nav.no"
		veileder.telefonnummer shouldBe "12345678"
	}

	"hentVeileder - veileder finnes - returnerer veileder uten alias" {
		val veilederRespons = """
			{
			  "data": {
				"ressurser": [
				  {
					"ressurs": {
					  "navIdent": "H156147",
					  "visningsNavn": null,
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

		val veileder = client.hentNavAnsatt("H156147") ?: fail("Veileder er null")

		veileder.navIdent shouldBe "H156147"
		veileder.navn shouldBe "Blaut Slappfisk"
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

		val veileder = client.hentNavAnsatt("H156147") ?: fail("Veileder er null")

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

		val veileder = client.hentNavAnsatt("test")
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

		client.hentNavAnsatt("test")

		val recordedRequest = server.takeRequest()

		recordedRequest.getHeader("Authorization") shouldBe "Bearer $token"
	}


	"hentVeileder - skal ikke hente kontortelefon først" {
		val kontorTelefon = "11111111"
		val tjenesteTelefon = "22222222"
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
					  "telefon": [
					  	{ "type": "NAV_TJENESTE_TELEFON", "nummer": "$tjenesteTelefon" },
					  	{ "type": "NAV_KONTOR_TELEFON", "nummer": "$kontorTelefon" }
					  ]
					},
					"code": "OK"
				  }
				]
			  }
		}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentNavAnsatt("H156147") ?: fail("Veileder finnes ikke")

		veileder.telefonnummer shouldBe kontorTelefon
	}

	"hentVeileder - skal ikke hente tjenestetelefon naar kontortelefon mangler" {
		val tjenesteTelefon = "22222222"
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
					  "telefon": [
					  	{ "type": "NAV_TJENESTE_TELEFON", "nummer": "$tjenesteTelefon" }
					  ]
					},
					"code": "OK"
				  }
				]
			  }
		}
		""".trimIndent()

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentNavAnsatt("H156147") ?: fail("Fant ikke veileder")

		veileder.telefonnummer shouldBe tjenesteTelefon
	}
})



