import io.kotest.core.spec.style.StringSpec
import no.nav.amt.tiltak.connectors.veilarboppfolging.VeilarboppfolgingClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.jupiter.api.assertThrows

const val token = "DUMMYTOKEN"

class VeilarboppfolgingClientTest: StringSpec({
    lateinit var server: MockWebServer
    lateinit var client: VeilarboppfolgingClient
	val veilederIdent = "V123"
	val fnr = "123"

    beforeTest {
        server = MockWebServer()
		val serverUrl = server.url("/api").toString()
        client = VeilarboppfolgingClient(serverUrl) { token }
	}

    "HentVeilederIdent - Bruker finnes - Returnerer veileder ident" {

		val respons = """{"veilederIdent": "$veilederIdent"}"""
		server.enqueue(MockResponse().setBody(respons))

        val veileder = client.hentVeilederIdent(fnr)
        assertEquals(veilederIdent, veileder)
    }

    "HentVeilederIdent - Manglende tilgang - Kaster exception" {
		server.enqueue(MockResponse().setResponseCode(401))
		assertThrows<RuntimeException> { client.hentVeilederIdent("123")  }
    }

	"HentVeilederIdent - Requester korrekt url" {
		val respons = """{"veilederIdent": "V123"}"""
		server.enqueue(MockResponse().setBody(respons))

		client.hentVeilederIdent(fnr)

		val request = server.takeRequest();

		assertEquals("/api/person/$fnr/veileder", request.path)
	}

	"HentVeilederIdent - Bruker finnes ikke - returnerer null" {
		val respons = """{"veilederIdent": null}"""
		server.enqueue(MockResponse().setBody(respons))

		val veileder = client.hentVeilederIdent(fnr)
		assertNull(veileder)
	}
})
