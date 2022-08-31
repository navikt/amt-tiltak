package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.amt.tiltak.test.integration.utils.KafkaMessageSender
import no.nav.amt.tiltak.test.integration.utils.SingletonKafkaProvider
import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@Profile("integration")
@TestConfiguration
open class IntegrationTestConfiguration(
	@Value("\${app.env.amtTiltakTopic}") private val tiltakTopic: String
) {

	@Bean
	open fun machineToMachineTokenClient(): MachineToMachineTokenClient = MachineToMachineTokenClient { "MOCK_TOKEN" }

	@Bean
	open fun kafkaProperties(): KafkaProperties = SingletonKafkaProvider.getKafkaProperties()

	@Bean
	open fun kafkaMessageSender(properties: KafkaProperties): KafkaMessageSender =
		KafkaMessageSender(properties, tiltakTopic)

}
