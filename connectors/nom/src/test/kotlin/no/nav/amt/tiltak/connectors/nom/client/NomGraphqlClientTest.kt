package no.nav.amt.tiltak.connectors.nom.client

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


class NomGraphqlClientTest : StringSpec({

	lateinit var server: MockWebServer
	lateinit var client: NomClient
	isolationMode = IsolationMode.InstancePerTest
	val expectedVeileder = Veileder("H156147", "Blaut", "Slappfisk", "")

	beforeTest {
		server = MockWebServer()

		client = NomClient(server.url("").toString(), { "dummytoken" })
	}

	"hentVeileder - veileder finnes - returnerer veileder" {
		val veilederRespons = """
		{
		  "data": {
			"ressurser": [
			  {
				"ressurs": {
				  "navIdent": "${expectedVeileder.navIdent}",
				  "fornavn": "${expectedVeileder.fornavn}",
				  "etternavn": "${expectedVeileder.etternavn}"
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
})



