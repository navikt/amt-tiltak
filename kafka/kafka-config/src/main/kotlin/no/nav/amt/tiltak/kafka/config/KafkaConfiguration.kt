package no.nav.amt.tiltak.kafka.config

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import no.nav.amt.tiltak.core.kafka.AmtArrangorIngestor
import no.nav.amt.tiltak.core.kafka.AnsattIngestor
import no.nav.amt.tiltak.core.kafka.ArenaAclIngestor
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.kafka.NavAnsattIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers.stringDeserializer
import no.nav.common.kafka.spring.PostgresJdbcTemplateConsumerRepository
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import java.util.function.Consumer


@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
open class KafkaConfiguration(
	kafkaTopicProperties: KafkaTopicProperties,
	kafkaProperties: KafkaProperties,
	jdbcTemplate: JdbcTemplate,
	arenaAclIngestor: ArenaAclIngestor,
	gjennomforingIngestor: GjennomforingIngestor,
	arrangorIngestor: AmtArrangorIngestor,
	ansattIngestor: AnsattIngestor,
	navAnsattIngestor: NavAnsattIngestor,
) {
	private val log = LoggerFactory.getLogger(javaClass)
    private var client: KafkaConsumerClient
	private var consumerRepository = PostgresJdbcTemplateConsumerRepository(jdbcTemplate)
	private var consumerRecordProcessor: KafkaConsumerRecordProcessor

	init {
		val topicConfigs = mutableListOf<KafkaConsumerClientBuilder.TopicConfig<String, out Any>>()

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.amtTiltakTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { arenaAclIngestor.ingestKafkaRecord(it.value()) }
				)
		)

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.sisteTiltaksgjennomforingerTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { gjennomforingIngestor.ingestKafkaRecord(it.key(), it.value()) }
				)
		)

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.amtArrangorTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { arrangorIngestor.ingestArrangor(it.value()) }
				)
		)

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.amtArrangorAnsattTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { ansattIngestor.ingestAnsatt(it.value()) }
				)
		)
		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.amtNavAnsattPersonaliaTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { navAnsattIngestor.ingest(it.value()) }
				)
		)

		consumerRecordProcessor = KafkaConsumerRecordProcessorBuilder
			.builder()
			.withRecordBatchSize(2000)
			.withLockProvider(JdbcTemplateLockProvider(jdbcTemplate))
			.withKafkaConsumerRepository(consumerRepository)
			.withConsumerConfigs(topicConfigs.map { it.consumerConfig })
			.build()

		client = KafkaConsumerClientBuilder.builder()
            .withProperties(kafkaProperties.consumer())
            .withTopicConfigs(topicConfigs.toList())
            .build()
    }

	@EventListener
	open fun onApplicationEvent(_event: ContextRefreshedEvent?) {
		log.info("Starting kafka consumer and stored record processor...")
		client.start()
		consumerRecordProcessor.start()
	}

}
