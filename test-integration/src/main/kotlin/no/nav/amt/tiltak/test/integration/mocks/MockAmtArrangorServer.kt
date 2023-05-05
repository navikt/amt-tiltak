package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockAmtArrangorServer : MockHttpServer("amt-arrangor-server") {

	fun addArrangorById(arrangor: AmtArrangorClient.ArrangorDto) = addResponseHandler(
		path = "/api/${arrangor.id}",
		response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(arrangor))
	)

	fun addArrangorByOrgNr(arrangor: AmtArrangorClient.ArrangorDto) = addResponseHandler(
		path = "/api/organisasjonsnummer/${arrangor.organisasjonsnummer}",
		response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(arrangor))

	)
}
