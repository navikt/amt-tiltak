package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.clients.pdl.PdlQueries
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import no.nav.amt.tiltak.tools.graphql.Graphql
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class MockPdlHttpServer : MockHttpServer(name = "PdlHttpServer") {

	fun reset() {
		resetHttpServer()
	}

	fun mockHentBruker(brukerFnr: String, mockPdlBruker: MockPdlBruker) {
		val request = toJsonString(
				Graphql.GraphqlQuery(
					PdlQueries.HentBruker.query,
					PdlQueries.HentBruker.Variables(brukerFnr)
				)
			)
		val requestPredicate = { req: RecordedRequest ->
			req.path == "/graphql"
				&& req.method == "POST"
				&& req.body.readUtf8() == request
		}

		addResponseHandler(requestPredicate, createPdlBrukerResponse(mockPdlBruker))
	}

	private fun createPdlBrukerResponse(mockPdlBruker: MockPdlBruker): MockResponse {
		val body = toJsonString(
			PdlQueries.HentBruker.Response(
				errors = null,
				data = PdlQueries.HentBruker.ResponseData(
					PdlQueries.HentBruker.HentPerson(
						navn = listOf(PdlQueries.HentBruker.Navn(mockPdlBruker.fornavn, null, mockPdlBruker.etternavn)),
						telefonnummer = listOf(PdlQueries.HentBruker.Telefonnummer("47", "12345678", 1)),
						adressebeskyttelse = if (mockPdlBruker.adressebeskyttelse != null) {
							listOf(PdlQueries.HentBruker.Adressebeskyttelse(mockPdlBruker.adressebeskyttelse.name))
						} else {
							emptyList()
						}
					)
				)
			)
		)

		return MockResponse()
			.setResponseCode(200)
			.setBody(body)
	}
}

data class MockPdlBruker(
	val fornavn: String = "Ola",
	val etternavn: String = "Nordmann",
	val adressebeskyttelse: AdressebeskyttelseGradering? = null,
)
