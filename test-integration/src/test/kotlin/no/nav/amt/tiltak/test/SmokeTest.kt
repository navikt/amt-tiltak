package no.nav.amt.tiltak.test

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.test.integration.IntegrationTestBase
import no.nav.amt.tiltak.test.integration.kafka.MessageCreator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class SmokeTest : IntegrationTestBase() {

	@BeforeEach
	internal fun setUp() {
		norgHttpClient.addDefaultData()
		poaoTilgangClient.addDefaultData()
		nomHttpClient.addDefaultData()
	}

	@Test
	internal fun firstTest() {
		val gjennomforingId = UUID.randomUUID()

		kafkaMessageSender.sendtTiltakTopic(
			MessageCreator.getGjennomforing(
				id = gjennomforingId,
				operation = Operation.CREATED,
				lopeNr = 1
			)
		)



		Thread.sleep(5000)

		val giTilgangResponse = sendRequest(
			method = "POST",
			url = "/api/tiltaksansvarlig/gjennomforing-tilgang?gjennomforingId=$gjennomforingId",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken()}"),
			body = "".toJsonRequestBody()
		)

		val response = sendRequest(
			method = "GET",
			url = "/api/nav-ansatt/gjennomforing",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken()}")
		)

		val body = response.body?.string()
		println(response)
	}
}
