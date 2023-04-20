package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.data_publisher.publish.*
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class DataPublisherService(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>,
	private val template: NamedParameterJdbcTemplate,
	private val enhetsregisterClient: EnhetsregisterClient,
	private val publishRepository: PublishRepository
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publish(id: UUID, type: DataPublishType) {
		when (type) {
			DataPublishType.ARRANGOR -> publishArrangor(id)
			DataPublishType.ARRANGOR_ANSATT -> publishArrangorAnsatt(id)
			DataPublishType.DELTAKER -> publishDeltaker(id)
			DataPublishType.DELTAKERLISTE -> publishDeltakerliste(id)
			DataPublishType.ENDRINGSMELDING -> publishEndringsmelding(id)
		}
	}

	fun publishAll(batchSize: Int = 100) {
		val idQueries = IdQueries(template)

		publishBatch(
			idProvider = { offset -> idQueries.hentDeltakerlisteIds(offset, batchSize) },
			publisher = { id -> publishDeltakerliste(id, true) }
		)

		publishBatch(
			idProvider = { offset -> idQueries.hentDeltakerIds(offset, batchSize) },
			publisher = { id -> publishDeltaker(id, true) }
		)

		publishBatch(
			idProvider = { offset -> idQueries.hentArrangorIds(offset, batchSize) },
			publisher = { id -> publishArrangor(id, true) }
		)

		publishBatch(
			idProvider = { offset -> idQueries.hentArrangorAnsattIds(offset, batchSize) },
			publisher = { id -> publishArrangorAnsatt(id, true) }
		)

		publishBatch(
			idProvider = { offset -> idQueries.hentEndringsmeldingIds(offset, batchSize) },
			publisher = { id -> publishEndringsmelding(id, true) }
		)
	}

	private fun publishDeltakerliste(id: UUID, forcePublish: Boolean = false) {
		val currentData = DeltakerlistePublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.DELTAKERLISTE, currentData.digest())) {
			val key = id.toString().toByteArray()
			val value = JsonUtils.toJsonString(currentData).toByteArray()
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorTopic, key, value)
			logger.info("Republiserer DELTAKERLISTE med id $id")
			kafkaProducerClient.sendSync(record)
			publishRepository.set(id, DataPublishType.DELTAKERLISTE, currentData.digest())
		}
	}

	private fun publishDeltaker(id: UUID, forcePublish: Boolean = false) {
		val currentData = DeltakerPublishQuery(template).get(id)

		if (currentData == null) {
			val key = id.toString().toByteArray()
			val record = ProducerRecord<ByteArray, ByteArray?>(kafkaTopicProperties.amtDeltakerTopic, key, null)
			logger.info("Legger inn Tombstone på DELTAKER med id $id")
			kafkaProducerClient.sendSync(record)
		} else if (forcePublish || !publishRepository.hasHash(id, DataPublishType.DELTAKER, currentData.digest())) {
			val key = id.toString().toByteArray()
			val value = JsonUtils.toJsonString(currentData).toByteArray()
			val record = ProducerRecord(kafkaTopicProperties.amtDeltakerTopic, key, value)
			logger.info("Republiserer DELTAKER med id $id")
			kafkaProducerClient.sendSync(record)
			publishRepository.set(id, DataPublishType.DELTAKER, currentData.digest())
		}

	}

	private fun publishArrangor(id: UUID, forcePublish: Boolean = false) {
		val currentData = ArrangorPublishQuery(template, enhetsregisterClient).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ARRANGOR, currentData.digest())) {
			val key = id.toString().toByteArray()
			val value = JsonUtils.toJsonString(currentData).toByteArray()
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorTopic, key, value)
			logger.info("Republiserer ARRANGOR med id $id")
			kafkaProducerClient.sendSync(record)
			publishRepository.set(id, DataPublishType.ARRANGOR, currentData.digest())
		}
	}

	private fun publishArrangorAnsatt(id: UUID, forcePublish: Boolean = false) {
		val currentData = ArrangorAnsattPublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ARRANGOR_ANSATT, currentData.digest())) {
			val key = id.toString().toByteArray()
			val value = JsonUtils.toJsonString(currentData).toByteArray()
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorAnsattTopic, key, value)
			logger.info("Republiserer ARRANGOR_ANSATT med id $id")
			kafkaProducerClient.sendSync(record)
			publishRepository.set(id, DataPublishType.ARRANGOR_ANSATT, currentData.digest())
		}
	}

	private fun publishEndringsmelding(id: UUID, forcePublish: Boolean = false) {
		val currentData = EndringsmeldingPublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ENDRINGSMELDING, currentData.digest())) {
			val key = id.toString().toByteArray()
			val value = JsonUtils.toJsonString(currentData).toByteArray()
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorAnsattTopic, key, value)
			logger.info("Republiserer ENDRINGSMELDING med id $id")
			kafkaProducerClient.sendSync(record)
			publishRepository.set(id, DataPublishType.ENDRINGSMELDING, currentData.digest())
		}
	}

	private fun publishBatch(
		idProvider: (offset: Int) -> List<UUID>,
		publisher: (id: UUID) -> Unit
	) {
		var offset = 0
		var ids: List<UUID>
		do {
			ids = idProvider.invoke(offset)
			ids.forEach { publisher.invoke(it) }
			offset += ids.size
		} while (ids.isNotEmpty())
	}
}
