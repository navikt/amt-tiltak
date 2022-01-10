package no.nav.amt.tiltak.application.configuration.kafka

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.ArenaAclIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers.stringDeserializer
import no.nav.common.kafka.spring.PostgresJdbcTemplateConsumerRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import java.util.function.Consumer

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
open class KafkaConfiguration(
    kafkaTopicProperties: KafkaTopicProperties,
	kafkaProperties: KafkaProperties,
	jdbcTemplate: JdbcTemplate,
	private val arenaAclIngestor: ArenaAclIngestor
) {
    private var client: KafkaConsumerClient
	private var consumerRepository = PostgresJdbcTemplateConsumerRepository(jdbcTemplate)
	private var consumerRecordProcessor: KafkaConsumerRecordProcessor

	init {
		val topicConfigs: List<KafkaConsumerClientBuilder.TopicConfig<String, String>> = listOf(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.amtTiltakTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { arenaAclIngestor.ingestKafkaMessageValue(it.value()) }
				)
		)

		consumerRecordProcessor = KafkaConsumerRecordProcessorBuilder
			.builder()
			.withLockProvider(JdbcTemplateLockProvider(jdbcTemplate))
			.withKafkaConsumerRepository(consumerRepository)
			.withConsumerConfigs(topicConfigs.map { it.consumerConfig })
			.build()

		client = KafkaConsumerClientBuilder.builder()
            .withProperties(kafkaProperties.consumer())
            .withTopicConfigs(topicConfigs)
            .build()

        client.start()
		consumerRecordProcessor.start()
    }

}
