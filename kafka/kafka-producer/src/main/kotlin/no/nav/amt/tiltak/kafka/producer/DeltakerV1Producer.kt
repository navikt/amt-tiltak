package no.nav.amt.tiltak.kafka.producer

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.kafka.DeltakerV1ProducerService
import no.nav.amt.tiltak.kafka.config.KafkaTopicProperties
import no.nav.amt.tiltak.kafka.producer.dto.DeltakerV1Dto
import no.nav.amt.tiltak.kafka.producer.dto.toDto
import no.nav.common.kafka.producer.KafkaProducerClient
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
open class DeltakerV1Producer(
	private val kafkaTopicProperties: KafkaTopicProperties,
	private val kafkaProducerClient: KafkaProducerClient<ByteArray, ByteArray>
) : DeltakerV1ProducerService {

	override fun publiserDeltaker(deltaker: Deltaker, endretDato: LocalDateTime) {
		val deltakerDto = DeltakerV1Dto(
			id = deltaker.id,
			gjennomforingId = deltaker.gjennomforingId,
			personIdent = deltaker.personIdent,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			status = deltaker.status.toDto(),
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			endretDato = endretDato,
			kilde = deltaker.kilde
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
