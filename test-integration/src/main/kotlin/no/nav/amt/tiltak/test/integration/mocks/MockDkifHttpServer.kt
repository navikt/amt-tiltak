package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockDkifHttpServer : MockHttpServer(name = "MockDkifHttpServer") {

	fun mockHentBrukerKontaktinformasjon(kontaktinformasjon: MockKontaktinformasjon) {
		val response = MockResponse()
				.setResponseCode(200)
				.setBody(JsonUtils.toJsonString(kontaktinformasjon))
		addResponseHandler("/rest/v1/person?inkluderSikkerDigitalPost=false", response)
	}
}

data class MockKontaktinformasjon(
	val epostadresse: String?,
	val mobiltelefonnummer: String?,
)

