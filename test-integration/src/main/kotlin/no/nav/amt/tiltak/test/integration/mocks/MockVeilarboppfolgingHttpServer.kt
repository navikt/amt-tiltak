package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockVeilarboppfolgingHttpServer : MockHttpServer(name = "MockVeilarboppfolgingHttpServer") {

	fun mockHentVeilederIdent(fnr: String, veilederIdent: String?) {
		val response = if (veilederIdent == null) {
			MockResponse().setResponseCode(204)
		} else {
			MockResponse()
				.setResponseCode(200)
				.setBody("""{"veilederIdent": "$veilederIdent"}""")
		}
		addResponseHandler("/veilarboppfolging/api/v2/veileder?fnr=$fnr", response)
	}
}
