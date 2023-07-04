package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.data_publisher.publish.DeltakerPublishQuery
import no.nav.amt.tiltak.data_publisher.publish.DeltakerlistePublishQuery
import no.nav.amt.tiltak.data_publisher.publish.EndringsmeldingPublishQuery
import no.nav.amt.tiltak.data_publisher.publish.IdQueries
import no.nav.amt.tiltak.data_publisher.publish.PublishRepository
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DataPublisherService(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val stringKafkaProducer: KafkaProducerClient<String, String>,
	private val template: NamedParameterJdbcTemplate,
	private val publishRepository: PublishRepository,
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publish(id: UUID, type: DataPublishType) {
		when (type) {
			DataPublishType.DELTAKER -> publishDeltaker(id)
			DataPublishType.DELTAKERLISTE -> publishDeltakerliste(id)
			DataPublishType.ENDRINGSMELDING -> publishEndringsmelding(id)
		}
	}

	fun publish(type: DataPublishType, batchSize: Int = 100, forcePublish: Boolean = true) {
		val idQueries = IdQueries(template)

		when (type) {
			DataPublishType.DELTAKER -> {
				publishBatch(
					idProvider = { offset -> idQueries.hentDeltakerIds(offset, batchSize) },
					publisher = { id -> publishDeltaker(id, forcePublish) }
				)
			}

			DataPublishType.DELTAKERLISTE -> {
				publishBatch(
					idProvider = { offset -> idQueries.hentDeltakerlisteIds(offset, batchSize) },
					publisher = { id -> publishDeltakerliste(id, forcePublish) }
				)

			}

			DataPublishType.ENDRINGSMELDING -> {
				publishBatch(
					idProvider = { offset -> idQueries.hentEndringsmeldingIds(offset, batchSize) },
					publisher = { id -> publishEndringsmelding(id, forcePublish) }
				)
			}
		}
	}

	fun publishAll(batchSize: Int = 100, forcePublish: Boolean = true) {
		DataPublishType.values().forEach {
			publish(
				type = it,
				batchSize = batchSize,
				forcePublish = forcePublish
			)
		}
	}

	private fun publishDeltakerliste(id: UUID, forcePublish: Boolean = false) {
		val deltakerlistePublishDto = DeltakerlistePublishQuery(template).get(id)

		if (deltakerlistePublishDto == null) {
			val key = id.toString()
			val record = ProducerRecord<String, String?>(kafkaTopicProperties.amtDeltakerlisteTopic, key, null)
			stringKafkaProducer.sendSync(record)
			logger.info("Tombstonet DELTAKERLISTE med id $id")
		} else {
			if (forcePublish || !publishRepository.hasHash(
					id,
					DataPublishType.DELTAKERLISTE,
					deltakerlistePublishDto.digest()
				)
			) {
				val key = id.toString()
				val value = JsonUtils.toJsonString(deltakerlistePublishDto)
				val record = ProducerRecord(kafkaTopicProperties.amtDeltakerlisteTopic, key, value)
				logger.info("Republiserer DELTAKERLISTE med id $id")
				stringKafkaProducer.sendSync(record)
				publishRepository.set(id, DataPublishType.DELTAKERLISTE, deltakerlistePublishDto.digest())
			}
		}
	}

	private fun publishDeltaker(id: UUID, forcePublish: Boolean = false) {
		when (val result = DeltakerPublishQuery(template).get(id)) {
			is DeltakerPublishQuery.Result.DontPublish -> return
			is DeltakerPublishQuery.Result.PublishTombstone -> {
				ProducerRecord<String, String?>(kafkaTopicProperties.amtDeltakerTopic, id.toString(), null)
					.let { stringKafkaProducer.sendSync(it) }
					.also { logger.info("Legger inn Tombstone på DELTAKER med id $id") }
			}

			is DeltakerPublishQuery.Result.OK -> {
				if (forcePublish || !publishRepository.hasHash(id, DataPublishType.DELTAKER, result.result.digest())) {
					ProducerRecord(
						kafkaTopicProperties.amtDeltakerTopic,
						id.toString(),
						JsonUtils.toJsonString(result.result)
					)
						.also { logger.info("Republiserer DELTAKER med id $id") }
						.also { stringKafkaProducer.sendSync(it) }
						.also { publishRepository.set(id, DataPublishType.DELTAKER, result.result.digest()) }
				}
			}
		}
	}

	private fun publishEndringsmelding(id: UUID, forcePublish: Boolean = false) {

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
