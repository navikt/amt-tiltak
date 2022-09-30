package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.pdl.PdlQueries
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.tools.graphql.Graphql
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockPdlHttpClient : MockHttpClient(name = "PdlHttpClient") {

	fun reset() {
		resetHttpServer()
	}

	fun addPdlBruker(brukerFnr: String, fornavn: String, etternavn: String) {
		val requestPredicate = { req: RecordedRequest ->
			req.path == "/graphql"
				&& req.method == "POST"
				&& req.body.readUtf8() == createPdlBrukerRequest(brukerFnr)
		}

		addResponseHandler(requestPredicate, createPdlBrukerResponse(fornavn, etternavn))
	}

	fun createPdlBrukerRequest(brukerFnr: String): String {
		return toJsonString(
			Graphql.GraphqlQuery(
				PdlQueries.HentBruker.query,
				PdlQueries.HentBruker.Variables(brukerFnr)
			)
		)
	}

	fun createPdlBrukerResponse(fornavn: String, etternavn: String): MockResponse {
		val body = toJsonString(
			PdlQueries.HentBruker.Response(
				errors = null,
				data = PdlQueries.HentBruker.ResponseData(
					PdlQueries.HentBruker.HentPerson(
						navn = listOf(PdlQueries.HentBruker.Navn(fornavn, null, etternavn)),
						telefonnummer = listOf(PdlQueries.HentBruker.Telefonnummer("47", "12345678", 1)),
						adressebeskyttelse = listOf(PdlQueries.HentBruker.Adressebeskyttelse("NEI"))
					)
				)
			)
		)

		return MockResponse()
			.setResponseCode(200)
			.setBody(body)
	}

}
