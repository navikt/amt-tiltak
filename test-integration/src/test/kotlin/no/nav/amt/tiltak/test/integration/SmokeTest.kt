package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.test.integration.kafka.MessageCreator
import no.nav.amt.tiltak.tiltak.repositories.GjennomforingRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class SmokeTest : IntegrationTestBase() {

	@Autowired
	lateinit var gjennomforingRepository: GjennomforingRepository

	@BeforeEach
	internal fun setUp() {
		norgHttpClient.addDefaultData()
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

		val response = sendRequest(
			method = "GET",
			path = "/api/nav-ansatt/gjennomforing",
			headers = mapOf("Authorization" to "Bearer ${oAuthServer.issueAzureAdToken()}")
		)

		val body = response.body?.string()
		println(response)
	}
}
