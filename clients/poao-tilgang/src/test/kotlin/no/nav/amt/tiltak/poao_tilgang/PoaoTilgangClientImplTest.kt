package no.nav.amt.tiltak.poao_tilgang

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClientImpl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.*

class PoaoTilgangClientImplTest : FunSpec({

	val server = MockWebServer()
	val serverUrl = server.url("").toString().removeSuffix("/")

	afterSpec {
		server.shutdown()
	}

	test("hentAdGrupper - skal lage riktig request og parse respons") {
		val client = PoaoTilgangClientImpl(
			baseUrl = serverUrl,
			tokenProvider = { "TOKEN" },
		)

		val navIdent = "Z12312"

		val gruppe1Id = UUID.randomUUID()
		val gruppe2Id = UUID.randomUUID()

		server.enqueue(
			MockResponse().setBody(
				"""
					[
						{"id": "$gruppe1Id", "name": "Gruppe1"},
						{"id": "$gruppe2Id", "name": "Gruppe2"}
					]
				""".trimIndent()
			)
		)

		val adGrupper = client.hentAdGrupper(navIdent)

		adGrupper shouldHaveSize 2
		adGrupper.any { it.id == gruppe1Id && it.name == "Gruppe1" }
		adGrupper.any { it.id == gruppe2Id && it.name == "Gruppe2" }

		val request = server.takeRequest()

		request.path shouldBe "/api/v1/ad-gruppe"
		request.method shouldBe "POST"
		request.getHeader("Authorization") shouldBe "Bearer TOKEN"
	}

})
