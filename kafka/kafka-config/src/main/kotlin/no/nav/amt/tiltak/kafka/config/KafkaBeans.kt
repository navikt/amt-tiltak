package no.nav.amt.tiltak.kafka.config

import no.nav.common.kafka.producer.KafkaProducerClient
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import no.nav.common.kafka.util.KafkaPropertiesPreset
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.Serializer
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
                return KafkaPropertiesPreset.aivenDefaultConsumerProperties("amt-tiltak-consumer.v10")
            }

            override fun producer(): Properties {
				return KafkaPropertiesPreset.aivenByteProducerProperties("amt-tiltak-producer")
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
				return KafkaPropertiesBuilder.producerBuilder()
					.withBrokerUrl("localhost:9092")
					.withBaseProperties()
					.withProducerId("amt-tiltak-producer")
					.withSerializers(ByteArraySerializer::class.java, ByteArraySerializer::class.java)
					.build()
            }
        }
    }

	@Bean
	open fun kafkaProducer(kafkaProperties: KafkaProperties): KafkaProducerClient<ByteArray, ByteArray> {
		return KafkaProducerClientImpl(kafkaProperties.producer())
	}

	@Bean(name = ["stringKafkaProducer"])
	@Profile("default")
	open fun stringKafkaProducer(kafkaProperties: KafkaProperties): KafkaProducerClient<String, String> {
		val properties = KafkaPropertiesPreset.aivenDefaultProducerProperties("amt-tiltak-producer-2")
		return KafkaProducerClientImpl(properties)
	}
}
