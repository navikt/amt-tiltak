package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.database.data.inputs.ArrangorInput
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import java.util.*

class MockAmtEnhetsregisterServer : MockHttpServer(name = "MockAmtEnhetsregisterServer") {

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

		if (enhet.overordnetEnhetOrganisasjonsnummer != null) {
			val overordnetEnhetsregisterEnhet = EnhetDto(
				organisasjonsnummer = enhet.overordnetEnhetOrganisasjonsnummer!!,
				navn = enhet.overordnetEnhetNavn!!,
				overordnetEnhetOrganisasjonsnummer = null,
				overordnetEnhetNavn = null
			)

			val overordnetEnhetResponse = MockResponse()
				.setResponseCode(200)
				.setBody(JsonUtils.toJsonString(overordnetEnhetsregisterEnhet))

			addResponseHandler("/api/enhet/${overordnetEnhetsregisterEnhet.organisasjonsnummer}", overordnetEnhetResponse)
		}
	}
}
