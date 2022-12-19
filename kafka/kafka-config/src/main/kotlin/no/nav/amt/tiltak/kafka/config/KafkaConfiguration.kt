package no.nav.amt.tiltak.kafka.config

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import no.nav.amt.tiltak.core.kafka.ArenaAclIngestor
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.kafka.SkjermetPersonIngestor
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.kafka.TildeltVeilederIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.feilhandtering.KafkaConsumerRecordProcessor
import no.nav.common.kafka.consumer.feilhandtering.util.KafkaConsumerRecordProcessorBuilder
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers.stringDeserializer
import no.nav.common.kafka.producer.KafkaProducerClient
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.spring.PostgresJdbcTemplateConsumerRepository
import okhttp3.internal.toImmutableList
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
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
	tildeltVeilederIngestor: TildeltVeilederIngestor,
	endringPaaBrukerIngestor: EndringPaaBrukerIngestor,
	skjermetPersonIngestor: SkjermetPersonIngestor,
	gjennomforingIngestor: GjennomforingIngestor,
) {
	private val log = LoggerFactory.getLogger(javaClass)
    private var client: KafkaConsumerClient
	private var consumerRepository = PostgresJdbcTemplateConsumerRepository(jdbcTemplate)
	private var consumerRecordProcessor: KafkaConsumerRecordProcessor

	init {
		val topicConfigs = mutableListOf<KafkaConsumerClientBuilder.TopicConfig<String, String>>()

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
					kafkaTopicProperties.sisteTilordnetVeilederTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { tildeltVeilederIngestor.ingestKafkaRecord(it.value()) }
				)
		)

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.endringPaaBrukerTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { endringPaaBrukerIngestor.ingestKafkaRecord(it.value()) }
				)
		)

		topicConfigs.add(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withStoreOnFailure(consumerRepository)
				.withConsumerConfig(
					kafkaTopicProperties.skjermedePersonerTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { skjermetPersonIngestor.ingest(it.key(), it.value()) }
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
					Consumer<ConsumerRecord<String, String>> { gjennomforingIngestor.ingestKafkaRecord(it.value()) }
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
            .withTopicConfigs(topicConfigs.toImmutableList())
            .build()
    }

	@Bean
	open fun kafkaProducer(kafkaProperties: KafkaProperties): KafkaProducerClient<String, String> {
		return KafkaProducerClientImpl(kafkaProperties.producer())
	}

	@EventListener
	open fun onApplicationEvent(_event: ContextRefreshedEvent?) {
		log.info("Starting kafka consumer and stored record processor...")
		client.start()
		consumerRecordProcessor.start()
	}

}
