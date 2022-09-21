package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.poao_tilgang.PoaoTilgangClientImpl
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.tilgangskontroll.ad_gruppe.AdGrupper.TILTAKSANSVARLIG_FLATE_GRUPPE
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory
import java.util.*

class MockPoaoTilgangHttpClient : MockHttpClient(name = "MockPoaoTilgangHttpClient") {


	private val logger = LoggerFactory.getLogger(javaClass)

	fun reset() {
		resetHttpServer()
	}

	fun addDefaultData() {
		addHentAdGrupperResponse(UUID.fromString("e2bae1e5-94c8-4ef6-9d7a-4d2e04b5ae1c"), TILTAKSANSVARLIG_FLATE_GRUPPE)
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

	fun addHentAdGrupperResponse(navAnsattAzureId: UUID? = null, name: String) {
		val url = "/api/v1/ad-gruppe"

		logger.info("Adding response for $url, navAnsattAzureId: $navAnsattAzureId")

		val predicate = { req: RecordedRequest ->
			if (navAnsattAzureId != null) {
				req.path == url
					&& req.method == "POST"
					&& req.body.readUtf8() == """{"navAnsattAzureId":"$navAnsattAzureId"}"""

			} else {
				req.path == url
					&& req.method == "POST"
			}
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
