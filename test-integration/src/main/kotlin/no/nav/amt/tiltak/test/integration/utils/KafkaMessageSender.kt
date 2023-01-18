package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaMessageSender(
	properties: KafkaProperties,
	@Value("\${app.env.amtTiltakTopic}")
	private val amtTiltakTopic: String,
	@Value("\${app.env.sisteTiltaksgjennomforingerTopic}")
	private val sisteTiltaksgjennomforingerTopic: String,
	@Value("\${app.env.sisteTilordnetVeilederTopic}")
	private val sisteTilordnetVeilederTopic: String,
	@Value("\${app.env.arenaTiltakDeltakerTopic}")
	private val arenaTiltakDeltakerTopic: String,
	@Value("\${app.env.endringPaaBrukerTopic}")
	private val endringPaaBrukerTopic: String,
	@Value("\${app.env.skjermedePersonerTopic}")
	private val skjermedePersonerTopic: String,
) {
	private val kafkaProducer = KafkaProducerClientImpl<String, String>(properties.producer())

	fun sendTilAmtTiltakTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(amtTiltakTopic, UUID.randomUUID().toString(), jsonString))
	}

	fun sendTilSisteTiltaksgjennomforingTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(sisteTiltaksgjennomforingerTopic, UUID.randomUUID().toString(), jsonString))
	}

	fun sendDeleteTilSisteTiltaksgjennomforingTopic(gjennomforingId: String) {
		kafkaProducer.send(ProducerRecord(sisteTiltaksgjennomforingerTopic, gjennomforingId, null))
	}

	fun sendTilSkjermetPersonTopic(fnr: String, erSkjermet: Boolean) {
		kafkaProducer.send(ProducerRecord(skjermedePersonerTopic, fnr, erSkjermet.toString()))
	}

}
