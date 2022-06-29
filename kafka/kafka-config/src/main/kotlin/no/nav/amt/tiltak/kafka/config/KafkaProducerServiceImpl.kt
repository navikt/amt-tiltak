package no.nav.amt.tiltak.kafka.config

import no.nav.amt.tiltak.common.json.JsonUtils.toJsonString
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.kafka.NavEnhetKafkaDto
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service

@Service
open class KafkaProducerServiceImpl(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<String, String>
) : KafkaProducerService {

	override fun sendNavEnhet(navEnhetKafkaDto: NavEnhetKafkaDto) {
		val key = navEnhetKafkaDto.enhetId
		val value = toJsonString(navEnhetKafkaDto)
		val record = ProducerRecord(kafkaTopicProperties.navEnhetTopic, key, value)

		kafkaProducerClient.sendSync(record)
	}

}
