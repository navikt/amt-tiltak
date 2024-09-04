package no.nav.amt.tiltak.data_publisher

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.port.UnleashService
import no.nav.amt.tiltak.data_publisher.model.DataPublishType
import no.nav.amt.tiltak.data_publisher.publish.DeltakerPublishQuery
import no.nav.amt.tiltak.data_publisher.publish.EndringsmeldingPublishQuery
import no.nav.amt.tiltak.data_publisher.publish.IdQueries
import no.nav.amt.tiltak.data_publisher.publish.PublishRepository
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class DataPublisherService(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val stringKafkaProducer: KafkaProducerClient<String, String>,
	private val template: NamedParameterJdbcTemplate,
	private val publishRepository: PublishRepository,
	private val unleashService: UnleashService
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	fun publish(id: UUID, type: DataPublishType) {
		when (type) {
			DataPublishType.DELTAKER -> publishDeltaker(id)
			DataPublishType.ENDRINGSMELDING -> publishEndringsmelding(id)
		}
	}

	fun publish(type: DataPublishType) {
		when (type) {
			DataPublishType.DELTAKER -> publish(type, fromDate = LocalDateTime.MIN)
			DataPublishType.ENDRINGSMELDING -> publish(type, fromDate = LocalDateTime.MIN)
		}

	}

	fun publish(type: DataPublishType, batchSize: Int = 100, forcePublish: Boolean = true, fromDate: LocalDateTime) {
		val idQueries = IdQueries(template)

		when (type) {
			DataPublishType.DELTAKER -> {
				publishBatch(
					idProvider = { offset -> idQueries.hentDeltakerIds(offset, batchSize, fromDate) },
					publisher = { id -> publishDeltaker(id, forcePublish) }
				)
			}

			DataPublishType.ENDRINGSMELDING -> {
				publishBatch(
					idProvider = { offset -> idQueries.hentEndringsmeldingIds(offset, batchSize, fromDate) },
					publisher = { id -> publishEndringsmelding(id, forcePublish) }
				)
			}
		}
	}

	fun publishAll(
		batchSize: Int = 100,
		forcePublish: Boolean = true,
		fromDate: LocalDateTime = LocalDateTime.now().minusDays(7)
	) {
		DataPublishType.entries.forEach {
			publish(
				type = it,
				batchSize = batchSize,
				forcePublish = forcePublish,
				fromDate = fromDate
			)
		}
	}

	fun publishDeltaker(id: UUID, forcePublish: Boolean = false) {
		when (val result = DeltakerPublishQuery(template, unleashService).get(id)) {
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

		if (currentData == null) {
			val record = ProducerRecord<String, String?>(kafkaTopicProperties.amtEndringsmeldingTopic, id.toString(), null)
			stringKafkaProducer.sendSync(record)
			logger.info("Legger inn tombstone på ENDRINGSMELDING med id $id")
			return
		}

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
