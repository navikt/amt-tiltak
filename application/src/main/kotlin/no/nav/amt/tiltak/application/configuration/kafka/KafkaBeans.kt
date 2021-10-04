package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*

@Configuration
open class KafkaBeans {

    @Bean
    @Profile("default")
    open fun kafkaConsumerProperties(): KafkaProperties {
        return object : KafkaProperties {
            override fun consumer(): Properties {
                return KafkaPropertiesPreset.aivenDefaultConsumerProperties("amt-tiltak-consumer")
            }

            override fun producer(): Properties {
                throw NotImplementedError("Not yet implemented")
            }
        }
    }

}
