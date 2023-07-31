package no.nav.amt.tiltak.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
	val amtTiltakTopic: String,
	val sisteTilordnetVeilederTopic: String,
	val skjermedePersonerTopic: String,
	val sisteTiltaksgjennomforingerTopic: String,
	val leesahTopic: String,
	val deltakerTopic: String,
	val aktorV2Topic: String,

	//Internal publish topics
	val amtArrangorTopic: String,
	val amtArrangorAnsattTopic: String,
	val amtDeltakerTopic: String,
	val amtDeltakerlisteTopic: String,
	val amtEndringsmeldingTopic: String
)
