package no.nav.amt.tiltak.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
	var amtTiltakTopic: String = "",
	var sisteTilordnetVeilederTopic: String = "",
	var endringPaaBrukerTopic: String = "",
)
