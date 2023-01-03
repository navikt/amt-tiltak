package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.mulighetsrommet_api_client.GjennomforingArenaData
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import java.util.*

class MockMulighetsrommetApiServer : MockHttpServer(name = "MockMulighetsrommetApiServer") {

	fun reset() {
		resetHttpServer()
	}

	fun gjennomforingArenaData(id: UUID, arenaDataResponse: GjennomforingArenaData) {
		val body = """
			{
				"opprettetAar": ${arenaDataResponse.opprettetAar},
				"lopenr": ${arenaDataResponse.lopenr},
				"virksomhetsnummer": "${arenaDataResponse.virksomhetsnummer}",
				"ansvarligNavEnhetId": "${arenaDataResponse.ansvarligNavEnhetId}",
				"status": "${arenaDataResponse.status}"
			}
		""".trimIndent()

		val response = MockResponse().setResponseCode(200).setBody(body)
		addResponseHandler("/api/v1/tiltaksgjennomforinger/arenadata/${id}", response)
	}
}
