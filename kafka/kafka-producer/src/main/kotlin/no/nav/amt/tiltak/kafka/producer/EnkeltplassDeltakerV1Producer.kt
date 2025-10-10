package no.nav.amt.tiltak.kafka.producer

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.kafka.EnkeltplassDeltakerProducerService
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service
import java.util.UUID

@Service
open class EnkeltplassDeltakerV1Producer(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>
): EnkeltplassDeltakerProducerService {

	override fun publiserDeltaker(deltaker: Deltaker) {
		val key = deltaker.id.toString().toByteArray()
		val value = JsonUtils.toJsonString(deltaker).toByteArray()

		val record = ProducerRecord(kafkaTopicProperties.amtEnkeltplassDeltakerTopic, key, value)

		kafkaProducerClient.sendSync(record)
	}

	override fun publiserSlettDeltaker(deltakerId: UUID) {
		val key = deltakerId.toString().toByteArray()

		val record = ProducerRecord<ByteArray, ByteArray?>(kafkaTopicProperties.amtEnkeltplassDeltakerTopic, key, null)

		kafkaProducerClient.sendSync(record)
	}
}
