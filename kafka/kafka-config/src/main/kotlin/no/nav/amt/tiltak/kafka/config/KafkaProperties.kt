package no.nav.amt.tiltak.kafka.config

import java.util.*

interface KafkaProperties {

    fun consumer(): Properties

    fun producer(): Properties

}
