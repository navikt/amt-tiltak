package no.nav.amt.tiltak.clients.mulighetsrommet_api_client

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import java.util.UUID

class MulighetsrommetApiClientImplTest {
	companion object {
		private val server = MockWebServer()
		private val serverUrl = server.url("").toString().removeSuffix("/")

		@JvmStatic
		@AfterAll
		fun teardown() {
			server.shutdown()
		}
	}

	@Test
	fun `hentGjennomforingArenaData - respons inneholder data - skal lage riktig request og parse respons`() {
		val client = MulighetsrommetApiClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		server.enqueue(
			MockResponse().setBody(
				"""
					{
						"opprettetAar": 2022,
						"lopenr": 123,
						"virksomhetsnummer": "999222333",
						"ansvarligNavEnhetId": "1234",
						"status": "GJENNOMFORES"
					}
				""".trimIndent()
			)
		)

		val id = UUID.randomUUID()
		val gjennomforingArenaData = client.hentGjennomforingArenaData(id)

		val request = server.takeRequest()

		gjennomforingArenaData shouldNotBe null
		gjennomforingArenaData?.opprettetAar shouldBe 2022
		gjennomforingArenaData?.lopenr shouldBe 123
		gjennomforingArenaData?.virksomhetsnummer shouldBe "999222333"
		gjennomforingArenaData?.ansvarligNavEnhetId shouldBe "1234"
		gjennomforingArenaData?.status shouldBe "GJENNOMFORES"

		request.path shouldBe "/api/v1/tiltaksgjennomforinger/arenadata/$id"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

	@Test
	fun `hentGjennomforingArenaData - respons har tom body - skal lage riktig request og parse respons`() {
		val client = MulighetsrommetApiClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" }
		)

		server.enqueue(
			MockResponse().setBody("")
		)

		val id = UUID.randomUUID()
		val gjennomforingArenaData = client.hentGjennomforingArenaData(id)

		val request = server.takeRequest()

		gjennomforingArenaData shouldBe null

		request.path shouldBe "/api/v1/tiltaksgjennomforinger/arenadata/$id"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}
}
