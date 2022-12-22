package no.nav.amt.tiltak.clients.mr_arena_adapter_client

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.*

class MulighetsrommetArenaClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentGjennomforingArenaData - skal lage riktig request og parse respons") {
		val client = MrArenaAdapterClientImpl(
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

		gjennomforingArenaData.opprettetAar shouldBe 2022
		gjennomforingArenaData.lopenr shouldBe 123
		gjennomforingArenaData.virksomhetsnummer shouldBe "999222333"
		gjennomforingArenaData.ansvarligNavEnhetId shouldBe "1234"
		gjennomforingArenaData.status shouldBe "GJENNOMFORES"

		request.path shouldBe "/TODO/$id"
		request.method shouldBe "GET"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

})

