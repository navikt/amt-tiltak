import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.veilarboppfolging.VeilarboppfolgingClientImpl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.assertThrows

const val token = "DUMMYTOKEN"

class VeilarboppfolgingClientTest: StringSpec({
    lateinit var server: MockWebServer
    lateinit var client: VeilarboppfolgingClientImpl
	val veilederIdent = "V123"
	val fnr = "123"

    beforeTest {
        server = MockWebServer()
		val serverUrl = server.url("/api").toString()
        client = VeilarboppfolgingClientImpl(serverUrl, { token })
	}

    "HentVeilederIdent - Bruker finnes - Returnerer veileder ident" {

		val jsonRepons = """{"veilederIdent":"V123"}""".trimIndent()
		server.enqueue(MockResponse().setBody(jsonRepons))

        val veileder = client.hentVeilederIdent(fnr)
		veileder shouldBe veilederIdent
    }

    "HentVeilederIdent - Manglende tilgang - Kaster exception" {
		server.enqueue(MockResponse().setResponseCode(401))
		assertThrows<RuntimeException> { client.hentVeilederIdent("123")  }
    }

	"HentVeilederIdent - Requester korrekt url" {
		val respons = """{"veilederIdent": "V123"}"""

		server.enqueue(MockResponse().setBody(respons))
		client.hentVeilederIdent(fnr)

		val request = server.takeRequest()

		request.path shouldBe "/api/api/v2/veileder?fnr=$fnr"
	}

	"HentVeilederIdent - Bruker finnes ikke - returnerer null" {
		server.enqueue(MockResponse().setResponseCode(204))
		val veileder = client.hentVeilederIdent(fnr)

		veileder shouldBe null
	}

})
