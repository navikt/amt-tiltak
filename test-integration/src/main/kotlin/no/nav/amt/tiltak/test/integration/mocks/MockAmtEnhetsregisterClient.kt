package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.common.json.JsonUtils
import okhttp3.mockwebserver.MockResponse

class MockAmtEnhetsregisterClient : MockHttpClient() {

	init {
		reset()
	}

	fun reset() {
		resetHttpServer()

		addEnhet(
			EnhetDto(
				organisasjonsnummer = "123456789",
				navn = "INTEGRATION_TEST_ENHET",
				overordnetEnhetOrganisasjonsnummer = "987654321",
				overordnetEnhetNavn = "INTEGRATION_TEST_OVERORDNET_ENHET"
			)
		)
	}

	fun addEnhet(enhet: EnhetDto) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(enhet))

		addResponseHandler("/api/enhet/${enhet.organisasjonsnummer}", response)
	}
}
