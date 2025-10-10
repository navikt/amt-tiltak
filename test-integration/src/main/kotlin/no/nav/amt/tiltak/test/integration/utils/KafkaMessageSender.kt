package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class KafkaMessageSender(
	properties: KafkaProperties,
	@Value("\${app.env.amtTiltakTopic}")
	private val amtTiltakTopic: String,
	@Value("\${app.env.sisteTiltaksgjennomforingerTopic}")
	private val sisteTiltaksgjennomforingerTopic: String,
	@Value("\${app.env.arenaTiltakDeltakerTopic}")
	private val arenaTiltakDeltakerTopic: String,
	@Value("\${app.env.amtArrangorTopic}")
	private val amtArrangorTopic: String,
	@Value("\${app.env.amtArrangorAnsattTopic}")
	private val amtArrangorAnsattTopic: String,
	@Value("\${app.env.amtNavBrukerPersonaliaTopic}")
	private val navBrukerTopic: String,
	@Value("\${app.env.amtNavAnsattPersonaliaTopic}")
	private val navAnsattTopic: String,
) {
	private val kafkaProducer = KafkaProducerClientImpl<ByteArray, ByteArray>(properties.producer())

	fun sendTilAmtTiltakTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(amtTiltakTopic, UUID.randomUUID().toString().toByteArray(), jsonString.toByteArray()))
	}

	fun sendTilSisteTiltaksgjennomforingTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(sisteTiltaksgjennomforingerTopic, UUID.randomUUID().toString().toByteArray(), jsonString.toByteArray()))
	}

	fun sendDeleteTilSisteTiltaksgjennomforingTopic(gjennomforingId: String) {
		kafkaProducer.send(ProducerRecord(sisteTiltaksgjennomforingerTopic, gjennomforingId.toByteArray(), null))
	}

	fun sendTilAmtArrangorTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(amtArrangorTopic, jsonString.toByteArray()))
	}

	fun sendTilAmtArrangorAnsattTopic(jsonString: String) {
		kafkaProducer.send(ProducerRecord(amtArrangorAnsattTopic, jsonString.toByteArray()))
	}

	fun sendTilNavAnsattTopic(key: UUID, jsonString: String?) {
		kafkaProducer.send(ProducerRecord(navAnsattTopic, key.toString().toByteArray(), jsonString?.toByteArray()))
	}

}
