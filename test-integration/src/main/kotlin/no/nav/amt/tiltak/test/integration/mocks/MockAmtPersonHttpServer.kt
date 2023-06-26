package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_person.NavAnsattRequest
import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import no.nav.amt.tiltak.test.integration.utils.getBodyAsString
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

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
}
