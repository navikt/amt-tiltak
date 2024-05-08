package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import java.util.UUID

class MockMulighetsrommetApiServer : MockHttpServer(name = "MockMulighetsrommetApiServer") {

	fun reset() {
		resetHttpServer()
	}

	fun gjennomforingArenaData(id: UUID, arenaDataResponse: GjennomforingArenaData?) {
		if (arenaDataResponse == null) {
			val response = MockResponse().setResponseCode(200)
			addResponseHandler("/api/v1/tiltaksgjennomforinger/arenadata/${id}", response)
		} else {
			val body = """
			{
				"opprettetAar": ${arenaDataResponse.opprettetAar},
				"lopenr": ${arenaDataResponse.lopenr}
			}
		""".trimIndent()

			val response = MockResponse().setResponseCode(200).setBody(body)
			addResponseHandler("/api/v1/tiltaksgjennomforinger/arenadata/${id}", response)
		}
	}
}
