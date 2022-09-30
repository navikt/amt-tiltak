package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord

class KafkaMessageSender(
	properties: KafkaProperties,
	private val amtTiltakTopic: String
) {
	private val kafkaProducer = KafkaProducerClientImpl<String, String>(properties.producer())

	fun sendtTiltakTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(amtTiltakTopic, jsonString))
	}
}
