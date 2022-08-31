import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.amt.tiltak.test.integration.utils.SingletonKafkaProvider
import no.nav.common.token_client.client.MachineToMachineTokenClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@Profile("integration")
@TestConfiguration
open class IntegrationTestConfiguration {

	@Bean
	open fun machineToMachineTokenClient(): MachineToMachineTokenClient {
		return MachineToMachineTokenClient { "MOCK_TOKEN" }
	}

	@Bean
	open fun kafkaProperties(): KafkaProperties {
		return SingletonKafkaProvider.getKafkaProperties()
	}

}
