package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.consumer.KafkaConsumerClient
import no.nav.common.kafka.consumer.util.KafkaConsumerClientBuilder
import no.nav.common.kafka.consumer.util.deserializer.Deserializers
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaMessageConsumer(
	private val kafkaTopicProperties: KafkaTopicProperties,
	kafkaProperties: KafkaProperties,
) {

	private val records = Collections.synchronizedList(mutableListOf<ConsumerRecord<String, String?>>())

	private val client: KafkaConsumerClient

	init {
		val properties = Properties()
		kafkaProperties.consumer().forEach{ properties[it.key] = it.value }
		properties[ConsumerConfig.GROUP_ID_CONFIG] = javaClass.canonicalName

		val configs = listOf(
			kafkaTopicProperties.deltakerTopic,
		).map(::createTopicConfig)

		client = KafkaConsumerClientBuilder.builder()
			.withProperties(properties)
			.withTopicConfigs(configs)
			.build()

		client.start()
	}

	fun getLatestRecord(topic: Topic):  ConsumerRecord<String, String?>? {
		return records.filter { it.topic() == mapKafkaTopic(topic) }.maxByOrNull { it.offset() }
	}

	fun reset() {
		records.clear()
	}

	private fun createTopicConfig(topic: String): KafkaConsumerClientBuilder.TopicConfig<String, String> {
		return KafkaConsumerClientBuilder.TopicConfig<String, String>()
			.withConsumerConfig(
				topic,
				Deserializers.stringDeserializer(),
				Deserializers.stringDeserializer(),
				::handleRecord
			)
	}

	private fun handleRecord(record: ConsumerRecord<String, String?>) {
		records.add(record)
	}

	private fun mapKafkaTopic(topic: Topic): String {
		return when(topic) {
			Topic.DELTAKER -> kafkaTopicProperties.deltakerTopic
		}
	}

	enum class Topic {
		DELTAKER,
	}

}

