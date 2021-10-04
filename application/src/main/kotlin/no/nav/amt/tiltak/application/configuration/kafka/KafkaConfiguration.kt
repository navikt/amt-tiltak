package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.amt.tiltak.ingestors.arena.ArenaIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers.stringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.function.Consumer

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
open class KafkaConfiguration(
    kafkaTopicProperties: KafkaTopicProperties,
	kafkaProperties: KafkaProperties,
    private val arenaIngestor: ArenaIngestor
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
		}

        client = KafkaConsumerClientBuilder.builder()
            .withProperties(kafkaProperties.consumer())
            .withTopicConfigs(topicConfigs)
            .build()

        client.start()
    }

}
