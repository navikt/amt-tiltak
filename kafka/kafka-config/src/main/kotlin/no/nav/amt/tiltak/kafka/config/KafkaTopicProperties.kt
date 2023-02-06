package no.nav.amt.tiltak.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
	val amtTiltakTopic: String,
	val sisteTilordnetVeilederTopic: String,
	val endringPaaBrukerTopic: String,
	val skjermedePersonerTopic: String,
	val sisteTiltaksgjennomforingerTopic: String,
	val leesahTopic: String,
	val deltakerTopic: String,
	val aktorV2Topic: String
)
