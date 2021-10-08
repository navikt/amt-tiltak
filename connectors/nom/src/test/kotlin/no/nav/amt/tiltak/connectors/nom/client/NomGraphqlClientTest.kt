package no.nav.amt.tiltak.connectors.nom.client

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer


class NomGraphqlClientTest : StringSpec({

	lateinit var server: MockWebServer
	lateinit var client: NomClient

	isolationMode = IsolationMode.InstancePerTest

	beforeTest {
		server = MockWebServer()
		server.enqueue(MockResponse().setBody(enVeileder))
		client = NomClient(server.url("").toString(), { "dummytoken" })
	}

	"happypath" {
		val veileder = client.hentVeileder("H156147")
		veileder.navIdent shouldBe "H156147"
	}

})

val enVeileder = """
{
  "data": {
    "ressurser": [
      {
        "ressurs": {
          "navIdent": "H156147",
		  "fornavn": "Blaut",
		  "etternavn": "Slappfisk"
        },
        "code": "OK"
      }
    ]
  }
}
"""
