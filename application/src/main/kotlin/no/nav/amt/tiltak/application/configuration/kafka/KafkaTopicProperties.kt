package no.nav.amt.tiltak.application.configuration.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class KafkaTopicProperties(
    var arenaTiltakTopic: String = "",
    var arenaTiltaksgruppeTopic: String = "",
    var arenaTiltakGjennomforingTopic: String = "",
    var arenaTiltakdeltakerTopic: String = "",
)
