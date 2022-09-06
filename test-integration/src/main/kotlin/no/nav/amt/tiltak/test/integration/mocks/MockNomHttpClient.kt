package no.nav.amt.tiltak.test.integration.mocks

import com.fasterxml.jackson.databind.JsonNode
import no.nav.amt.tiltak.clients.nom.NomQueries
import no.nav.amt.tiltak.common.json.JsonUtils
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockNomHttpClient : MockHttpClient() {

	fun reset() {
		resetHttpServer()
	}

	fun addDefaultData() {
		add(
			NomClientResponseInput(
				navIdent = "Z123",
				visningsNavn = "Jeg er en test",
				fornavn = "INTEGRASJON",
				etternavn = "TEST",
				epost = "integrasjon.test@nav.no",
				telefon = listOf(
					NomQueries.HentIdenter.Telefon("12345678", "NAV_TJENESTE_TELEFON")
				)
			)
		)
	}

	fun add(input: NomClientResponseInput) {
		val predicate = { req: RecordedRequest ->
			req.path == "/graphql"
				&& req.method == "POST"
				&& containsIdentifier(req, input.navIdent)
		}

		addResponseHandler(predicate, createResponse(input))
	}

	private fun containsIdentifier(req: RecordedRequest, identifier: String): Boolean {
		val body = JsonUtils.fromJsonString<JsonNode>(req.body.readUtf8())
		val identer = JsonUtils.fromJsonString<List<String>>(body.get("variables").get("identer").toString())
		return identer.contains(identifier)
	}

	private fun createResponse(input: NomClientResponseInput): MockResponse {
		val body = NomQueries.HentIdenter.Response(
			errors = emptyList(),
			data = NomQueries.HentIdenter.ResponseData(
				listOf(
					NomQueries.HentIdenter.RessursResult(
						code = NomQueries.HentIdenter.ResultCode.OK,
						ressurs = NomQueries.HentIdenter.Ressurs(
							navIdent = input.navIdent,
							visningsNavn = input.visningsNavn,
							fornavn = input.fornavn,
							etternavn = input.etternavn,
							epost = input.epost,
							telefon = input.telefon
						)
					)
				)
			)
		)

		return MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(body))
	}

	data class NomClientResponseInput(
		val navIdent: String,
		val visningsNavn: String?,
		val fornavn: String?,
		val etternavn: String?,
		val epost: String?,
		val telefon: List<NomQueries.HentIdenter.Telefon>,
	)
}
