package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClientImpl
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory
import java.util.*

class MockPoaoTilgangHttpServer : MockHttpServer(name = "MockPoaoTilgangHttpServer") {


	private val logger = LoggerFactory.getLogger(javaClass)

	fun reset() {
		resetHttpServer()
	}

	fun addErSkjermetResponse(data: Map<String, Boolean>) {
		val url = "/api/v1/skjermet-person/bulk"

		val predicate = { req: RecordedRequest ->
			val body = req.body.readUtf8()

			req.path == url
				&& req.method == "POST"
				&& data.keys.map { body.contains(it) }.all { true }
		}

		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(data))

		addResponseHandler(predicate, response)
	}

	fun addHentAdGrupperResponse(navAnsattAzureId: UUID, name: String) {
		val url = "/api/v1/ad-gruppe"

		logger.info("Adding response for $url, navAnsattAzureId: $navAnsattAzureId")

		val predicate = { req: RecordedRequest ->
			req.path == url
				&& req.method == "POST"
				&& req.body.readUtf8().contains(navAnsattAzureId.toString())
		}

		val response = MockResponse()
			.setResponseCode(200)
			.setBody(
				JsonUtils.toJsonString(
					listOf(
						PoaoTilgangClientImpl.HentAdGrupper.AdGruppeDto(
							id = UUID.randomUUID(),
							name = name
						)
					)
				)
			)

		addResponseHandler(predicate, response)
	}
}
