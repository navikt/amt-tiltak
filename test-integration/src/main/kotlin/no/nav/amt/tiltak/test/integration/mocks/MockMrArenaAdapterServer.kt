package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetDto
import no.nav.amt.tiltak.clients.mr_arena_adapter_client.GjennomforingArenaData
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse
import org.apache.kafka.common.protocol.types.Field
import java.util.UUID

class MockMrArenaAdapterServer : MockHttpServer(name = "MockMrArenaAdapterServer") {

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
		addResponseHandler("/TODO/${id}", response)
	}
}
