package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.clients.amt_enhetsregister.EnhetsregisterClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.data_publisher.publish.*
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class DataPublisherService(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val stringKafkaProducer: KafkaProducerClient<String, String>,
	private val template: NamedParameterJdbcTemplate,
	private val enhetsregisterClient: EnhetsregisterClient,
	private val publishRepository: PublishRepository,
	@Value("\${publish.arrangor:true}") private val publishArrangor: Boolean = true,
	@Value("\${publish.arrangorAnsatt:true}") private val publishArrangorAnsatt: Boolean = true,
	@Value("\${publish.deltaker:true}") private val publishDeltaker: Boolean = true,
	@Value("\${publish.deltakerliste:true}") private val publishDeltakerliste: Boolean = true,
	@Value("\${publish.endringsmelding:true}") private val publishEndringsmelding: Boolean = true
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

	fun publishAll(batchSize: Int = 100, forcePublish: Boolean = true) {
		val idQueries = IdQueries(template)

		if (publishDeltakerliste) {
			publishBatch(
				idProvider = { offset -> idQueries.hentDeltakerlisteIds(offset, batchSize) },
				publisher = { id -> publishDeltakerliste(id, forcePublish) }
			)
		}

		if (publishDeltaker) {
			publishBatch(
				idProvider = { offset -> idQueries.hentDeltakerIds(offset, batchSize) },
				publisher = { id -> publishDeltaker(id, forcePublish) }
			)

		}

		if (publishArrangor) {
			publishBatch(
				idProvider = { offset -> idQueries.hentArrangorIds(offset, batchSize) },
				publisher = { id -> publishArrangor(id, forcePublish) }
			)
		}

		if (publishArrangorAnsatt) {
			publishBatch(
				idProvider = { offset -> idQueries.hentArrangorAnsattIds(offset, batchSize) },
				publisher = { id -> publishArrangorAnsatt(id, forcePublish) }
			)
		}

		if (publishEndringsmelding) {
			publishBatch(
				idProvider = { offset -> idQueries.hentEndringsmeldingIds(offset, batchSize) },
				publisher = { id -> publishEndringsmelding(id, forcePublish) }
			)
		}
	}

	private fun publishDeltakerliste(id: UUID, forcePublish: Boolean = false) {
		if (!publishDeltakerliste) {
			logger.info("Publisering av deltakerlister er ikke skrudd på")
			return
		}

		val currentData = DeltakerlistePublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.DELTAKERLISTE, currentData.digest())) {
			val key = id.toString()
			val value = JsonUtils.toJsonString(currentData)
			val record = ProducerRecord(kafkaTopicProperties.amtDeltakerlisteTopic, key, value)
			logger.info("Republiserer DELTAKERLISTE med id $id")
			stringKafkaProducer.sendSync(record)
			publishRepository.set(id, DataPublishType.DELTAKERLISTE, currentData.digest())
		}
	}

	private fun publishDeltaker(id: UUID, forcePublish: Boolean = false) {
		if (!publishDeltaker) {
			logger.info("Publisering av deltakere er ikke skrudd på")
			return
		}

		val currentData = DeltakerPublishQuery(template).get(id)

		if (currentData == null) {
			val key = id.toString()
			val record = ProducerRecord<String, String?>(kafkaTopicProperties.amtDeltakerTopic, key, null)
			logger.info("Legger inn Tombstone på DELTAKER med id $id")
			stringKafkaProducer.sendSync(record)
		} else if (forcePublish || !publishRepository.hasHash(id, DataPublishType.DELTAKER, currentData.digest())) {
			val key = id.toString()
			val value = JsonUtils.toJsonString(currentData)
			val record = ProducerRecord(kafkaTopicProperties.amtDeltakerTopic, key, value)
			logger.info("Republiserer DELTAKER med id $id")
			stringKafkaProducer.sendSync(record)
			publishRepository.set(id, DataPublishType.DELTAKER, currentData.digest())
		}

	}

	private fun publishArrangor(id: UUID, forcePublish: Boolean = false) {
		if (!publishArrangor) {
			logger.info("Publisering av arangør er ikke skrudd på")
			return
		}

		val currentData = ArrangorPublishQuery(template, enhetsregisterClient).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ARRANGOR, currentData.digest())) {
			val key = id.toString()
			val value = JsonUtils.toJsonString(currentData)
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorTopic, key, value)
			logger.info("Republiserer ARRANGOR med id $id")
			stringKafkaProducer.sendSync(record)
			publishRepository.set(id, DataPublishType.ARRANGOR, currentData.digest())
		}
	}

	private fun publishArrangorAnsatt(id: UUID, forcePublish: Boolean = false) {
		if (!publishArrangorAnsatt) {
			logger.info("Publisering av Ansatte er ikke skrudd på")
			return
		}

		val currentData = ArrangorAnsattPublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ARRANGOR_ANSATT, currentData.digest())) {
			val key = id.toString()
			val value = JsonUtils.toJsonString(currentData)
			val record = ProducerRecord(kafkaTopicProperties.amtArrangorAnsattTopic, key, value)
			logger.info("Republiserer ARRANGOR_ANSATT med id $id")
			stringKafkaProducer.sendSync(record)
			publishRepository.set(id, DataPublishType.ARRANGOR_ANSATT, currentData.digest())
		}
	}

	private fun publishEndringsmelding(id: UUID, forcePublish: Boolean = false) {
		if (!publishEndringsmelding) {
			logger.info("Publisering av Endringsmeldinger er ikke skrudd på")
			return
		}

		val currentData = EndringsmeldingPublishQuery(template).get(id)

		if (forcePublish || !publishRepository.hasHash(id, DataPublishType.ENDRINGSMELDING, currentData.digest())) {
			val key = id.toString()
			val value = JsonUtils.toJsonString(currentData)
			val record = ProducerRecord(kafkaTopicProperties.amtEndringsmeldingTopic, key, value)
			logger.info("Republiserer ENDRINGSMELDING med id $id")
			stringKafkaProducer.sendSync(record)
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
