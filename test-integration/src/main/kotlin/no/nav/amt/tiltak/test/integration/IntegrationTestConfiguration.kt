package no.nav.amt.tiltak.test.integration

import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.integration.utils.SingletonKafkaProvider
import no.nav.common.kafka.producer.KafkaProducerClient
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Profile("integration")
@TestConfiguration
open class IntegrationTestConfiguration {

	@Bean
	open fun kafkaProperties(): KafkaProperties = SingletonKafkaProvider.getKafkaProperties(ByteArraySerializer(), ByteArraySerializer())

	@Bean(name = ["stringKafkaProducer"])
	open fun stringKafkaProducer(): KafkaProducerClient<String, String> {
		val properties = SingletonKafkaProvider.getKafkaProperties(StringSerializer(), StringSerializer()).producer()
		return KafkaProducerClientImpl(properties)
	}

	@Bean
	open fun testDataRepository(template: NamedParameterJdbcTemplate): TestDataRepository {
		return TestDataRepository(template)
	}

	@Bean
	open fun unleashClient(
	): Unleash {
		val fakeUnleash = FakeUnleash()
		fakeUnleash.enableAll()
		return fakeUnleash
	}
}
