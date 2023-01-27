package no.nav.amt.tiltak.kafka.producer

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.amt.tiltak.kafka.producer.dto.DeltakerV1Dto
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service
import java.util.*

@Service
open class KafkaProducerServiceImpl(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>
) : KafkaProducerService {

	override fun publiserDeltaker(deltaker: Deltaker) {
		val deltakerDto = DeltakerV1Dto(
			id = deltaker.id,
			gjennomforingId = deltaker.gjennomforingId,
			personIdent = deltaker.personIdent,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			status = deltaker.status.type,
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
		)

		val key = deltaker.id.toString().toByteArray()
		val value = JsonUtils.toJsonString(deltakerDto).toByteArray()

		val record = ProducerRecord(kafkaTopicProperties.deltakerTopic, key, value)

		kafkaProducerClient.sendSync(record)
	}

	override fun publiserSlettDeltaker(deltakerId: UUID) {
		val key = deltakerId.toString().toByteArray()

		val record = ProducerRecord<ByteArray, ByteArray?>(kafkaTopicProperties.deltakerTopic, key, null)

		kafkaProducerClient.sendSync(record)
	}

}
