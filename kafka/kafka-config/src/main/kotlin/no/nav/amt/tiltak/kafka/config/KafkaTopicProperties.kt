package no.nav.amt.tiltak.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
	val amtTiltakTopic: String,
	val sisteTiltaksgjennomforingerTopic: String,
	val deltakerTopic: String,
	val amtNavBrukerPersonaliaTopic: String,

	//Internal publish topics
	val amtArrangorTopic: String,
	val amtArrangorAnsattTopic: String,
	val amtDeltakerTopic: String,
	val amtDeltakerlisteTopic: String,
	val amtEndringsmeldingTopic: String
)
