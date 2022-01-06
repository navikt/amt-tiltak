package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.amt.tiltak.core.port.ArenaIngestor
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.ArenaAclIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers.stringDeserializer
import okhttp3.internal.toImmutableList
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
open class KafkaConfiguration(
    kafkaTopicProperties: KafkaTopicProperties,
	kafkaProperties: KafkaProperties,
    private val arenaIngestor: ArenaIngestor,
	private val arenaAclIngestor: ArenaAclIngestor
) {
    private var client: KafkaConsumerClient

    init {
		val topicConfigs = listOf(
			kafkaTopicProperties.arenaTiltakTopic,
			kafkaTopicProperties.arenaTiltaksgruppeTopic,
			kafkaTopicProperties.arenaTiltaksgjennomforingTopic,
			kafkaTopicProperties.arenaTiltakDeltakerTopic
		).map { topic ->
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withConsumerConfig(
					topic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { arenaIngestor.ingest(it.value()) }
				)
		}.toMutableList()

		topicConfigs.add(KafkaConsumerClientBuilder.TopicConfig<String, String>()
			.withLogging()
			.withConsumerConfig(
				kafkaTopicProperties.amtTiltakTopic,
				stringDeserializer(),
				stringDeserializer(),
				Consumer<ConsumerRecord<String, String>> { arenaAclIngestor.ingestKafkaMessageValue(it.value()) }
			))

        client = KafkaConsumerClientBuilder.builder()
            .withProperties(kafkaProperties.consumer())
            .withTopicConfigs(topicConfigs.toImmutableList())
            .build()

        client.start()
    }

}
