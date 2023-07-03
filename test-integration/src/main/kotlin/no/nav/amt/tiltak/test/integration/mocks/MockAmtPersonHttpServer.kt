package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_person.NavAnsattRequest
import no.nav.amt.tiltak.clients.amt_person.NavEnhetRequest
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import no.nav.amt.tiltak.test.integration.utils.getBodyAsString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

class MockAmtPersonHttpServer : MockHttpServer("MockAmtPersonHttpServer") {
	init {
		addResponseHandler("/api/migrer/nav-bruker", MockResponse().setResponseCode(200))
		addResponseHandler("/api/migrer/nav-enhet", MockResponse().setResponseCode(200))
	}

	fun addAnsattResponse(navAnsatt: NavAnsatt) {
		val request = toJsonString(NavAnsattRequest(navAnsatt.navIdent))

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/nav-ansatt"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(navAnsatt))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)
	}

	fun addNavEnhetResponse(navEnhetId: String, navn: String) {
		val request = toJsonString(NavEnhetRequest(navEnhetId))
		val enhet = NavEnhet(UUID.randomUUID(), navEnhetId, navn)

		val requestPredicate = { req: RecordedRequest ->
			req.path == "/api/nav-enhet"
				&& req.method == "POST"
				&& req.getBodyAsString() == request
		}

		val response = MockResponse()
			.setBody(toJsonString(enhet))
			.setResponseCode(200)

		addResponseHandler(requestPredicate, response)
	}
}
