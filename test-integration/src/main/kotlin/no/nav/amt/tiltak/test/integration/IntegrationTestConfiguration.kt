package no.nav.amt.tiltak.test.integration

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.amt.tiltak.test.database.data.TestDataRepository
import no.nav.amt.tiltak.test.integration.utils.SingletonKafkaProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

@Profile("integration")
@TestConfiguration
open class IntegrationTestConfiguration {

	@Bean
	open fun kafkaProperties(): KafkaProperties = SingletonKafkaProvider.getKafkaProperties()

	@Bean
	open fun testDataRepository(template: NamedParameterJdbcTemplate): TestDataRepository {
		return TestDataRepository(template)
	}

}
