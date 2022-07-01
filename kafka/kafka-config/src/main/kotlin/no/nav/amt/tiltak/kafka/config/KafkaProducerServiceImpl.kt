package no.nav.amt.tiltak.kafka.config

import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.common.kafka.producer.KafkaProducerClient
import org.springframework.stereotype.Service

@Service
open class KafkaProducerServiceImpl(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<String, String>
) : KafkaProducerService {}
