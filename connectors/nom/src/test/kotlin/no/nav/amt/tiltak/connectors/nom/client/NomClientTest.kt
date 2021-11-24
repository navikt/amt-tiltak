package no.nav.amt.tiltak.connectors.nom.client

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class NomGraphqlClientTest : StringSpec({

	val token = "DUMMYTOKEN"

	lateinit var server: MockWebServer
	lateinit var client: NomClient
	isolationMode = IsolationMode.InstancePerTest
	val expectedVeileder = Veileder("H156147", "Alias", "Blaut", "Slappfisk", "blaut.slappfisk@nav.no")

	beforeTest {
		server = MockWebServer()

		client = NomClient(server.url("").toString(), { token })
	}

	"hentVeileder - veileder finnes - returnerer veileder" {
		val veilederRespons = """
		{
		  "data": {
			"ressurser": [
			  {
				"ressurs": {
				  "navIdent": "${expectedVeileder.navIdent}",
				  "visningsNavn": "${expectedVeileder.visningsNavn}",
				  "fornavn": "${expectedVeileder.fornavn}",
				  "etternavn": "${expectedVeileder.etternavn}",
				  "epost": "${expectedVeileder.epost}"
				},
				"code": "OK"
			  }
			]
		  }
		}
		"""

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentVeileder(expectedVeileder.navIdent)
		veileder shouldNotBe null
		veileder!!.navIdent shouldBe expectedVeileder.navIdent
	}

	"hentVeileder - veileder finnes ikke - returnerer NOT_FOUND" {
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
		"""

		server.enqueue(MockResponse().setBody(veilederRespons))

		val veileder = client.hentVeileder(expectedVeileder.navIdent)
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
		"""

		server.enqueue(MockResponse().setBody(veilederRespons))

		client.hentVeileder(expectedVeileder.navIdent)

		val recordedRequest = server.takeRequest()

		recordedRequest.getHeader("Authorization") shouldBe "Bearer $token"
	}
})



