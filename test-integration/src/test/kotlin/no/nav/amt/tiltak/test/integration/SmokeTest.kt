package no.nav.amt.tiltak.test.integration

import io.kotest.matchers.shouldBe
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation
import no.nav.amt.tiltak.test.integration.kafka.MessageCreator
import org.junit.jupiter.api.Test

class SmokeTest : IntegrationTestBase() {

	@Test
	internal fun firstTest() {
		kafkaMessageSender.sendtTiltakTopic(MessageCreator.getGjennomforing(
			operation = Operation.CREATED,
			lopeNr = 1
		))

		Thread.sleep(100000000)
		true shouldBe true
	}
}
