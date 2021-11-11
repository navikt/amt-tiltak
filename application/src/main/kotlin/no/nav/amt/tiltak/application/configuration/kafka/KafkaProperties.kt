package no.nav.amt.tiltak.application.configuration.kafka

import java.util.*

interface KafkaProperties {

    fun consumer(): Properties

    fun producer(): Properties

}
