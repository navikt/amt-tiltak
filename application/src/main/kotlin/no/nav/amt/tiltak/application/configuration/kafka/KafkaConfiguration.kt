package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.ArenaAclIngestor
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
	private val arenaAclIngestor: ArenaAclIngestor
) {
    private var client: KafkaConsumerClient

    init {
		val topicConfigs: List<KafkaConsumerClientBuilder.TopicConfig<String, String>> = listOf(
			KafkaConsumerClientBuilder.TopicConfig<String, String>()
				.withLogging()
				.withConsumerConfig(
					kafkaTopicProperties.amtTiltakTopic,
					stringDeserializer(),
					stringDeserializer(),
					Consumer<ConsumerRecord<String, String>> { arenaAclIngestor.ingestKafkaMessageValue(it.value()) }
				)
		)

        client = KafkaConsumerClientBuilder.builder()
            .withProperties(kafkaProperties.consumer())
            .withTopicConfigs(topicConfigs)
            .build()

        client.start()
    }

}
