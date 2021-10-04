package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.amt.tiltak.ingestors.arena.ArenaIngestor
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.function.Consumer

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties::class)
open class KafkaConfiguration(
    kafkaTopicProperties: KafkaTopicProperties,
    private val arenaIngestor: ArenaIngestor
) {
    private var client: KafkaConsumerClient

    init {
        client = KafkaConsumerClientBuilder.builder()
            .withProperties(properties())
            .withTopicConfig(
                KafkaConsumerClientBuilder.TopicConfig<String, String>()
                    .withConsumerConfig(
                        kafkaTopicProperties.arenaTiltakTopic,
                        Deserializers.stringDeserializer(),
                        Deserializers.stringDeserializer(),
                        Consumer<ConsumerRecord<String, String>> { arenaIngestor.ingest(it.value()) }
                    )
                    .withConsumerConfig(
                        kafkaTopicProperties.arenaTiltaksgruppeTopic,
                        Deserializers.stringDeserializer(),
                        Deserializers.stringDeserializer(),
                        Consumer<ConsumerRecord<String, String>> { arenaIngestor.ingest(it.value()) }
                    )
                    .withConsumerConfig(
                        kafkaTopicProperties.arenaTiltakGjennomforingTopic,
                        Deserializers.stringDeserializer(),
                        Deserializers.stringDeserializer(),
                        Consumer<ConsumerRecord<String, String>> { arenaIngestor.ingest(it.value()) }
                    )
                    .withConsumerConfig(
                        kafkaTopicProperties.arenaTiltakdeltakerTopic,
                        Deserializers.stringDeserializer(),
                        Deserializers.stringDeserializer(),
                        Consumer<ConsumerRecord<String, String>> { arenaIngestor.ingest(it.value()) }
                    )
            )
            .build()

        client.start()
    }

    private fun properties(): Properties {
        return KafkaPropertiesPreset.aivenDefaultConsumerProperties("groupId")
    }

}
