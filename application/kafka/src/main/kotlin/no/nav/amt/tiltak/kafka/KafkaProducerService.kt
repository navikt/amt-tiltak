package no.nav.amt.tiltak.kafka

import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.kafka.dto.NavEnhetKafkaDto
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service

@Service
open class KafkaProducerService(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<String, String>
) {

	fun sendNavEnhet(navEnhetKafkaDto: NavEnhetKafkaDto) {
		val key = navEnhetKafkaDto.enhetId
		val value = toJsonString(navEnhetKafkaDto)
		val record = ProducerRecord(kafkaTopicProperties.navEnhetTopic, key, value)

		kafkaProducerClient.sendSync(record)
	}

}
