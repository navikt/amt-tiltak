package no.nav.amt.tiltak.application.configuration.kafka

import no.nav.common.kafka.util.KafkaPropertiesBuilder
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.apache.kafka.common.serialization.ByteArrayDeserializer
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
                return KafkaPropertiesPreset.aivenDefaultConsumerProperties("amt-tiltak-consumer.v8")
            }

            override fun producer(): Properties {
                throw NotImplementedError("Not yet implemented")
            }
        }
    }

    @Bean
    @Profile("local")
    open fun localKafkaConsumerProperties(): KafkaProperties {
        return object : KafkaProperties {
            override fun consumer(): Properties {
                return KafkaPropertiesBuilder.consumerBuilder()
                    .withBrokerUrl("localhost:9092")
                    .withBaseProperties()
                    .withConsumerGroupId("amt-tiltak-consumer.v1")
                    .withDeserializers(ByteArrayDeserializer::class.java, ByteArrayDeserializer::class.java)
                    .build()
            }

            override fun producer(): Properties {
                throw NotImplementedError("Not yet implemented")
            }
        }
    }

}
