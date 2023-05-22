package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockAmtPersonHttpServer : MockHttpServer("MockAmtPersonHttpServer") {
	init {
	    addResponseHandler("/api/migrer/arrangor-ansatt", MockResponse().setResponseCode(200))
		addResponseHandler("/api/migrer/nav-bruker", MockResponse().setResponseCode(200))
		addResponseHandler("/api/migrer/nav-ansatt", MockResponse().setResponseCode(200))
		addResponseHandler("/api/migrer/nav-enhet", MockResponse().setResponseCode(200))
	}
}
