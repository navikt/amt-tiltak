package no.nav.amt.tiltak.kafka

import java.util.*

interface KafkaProperties {

    fun consumer(): Properties

    fun producer(): Properties

}
