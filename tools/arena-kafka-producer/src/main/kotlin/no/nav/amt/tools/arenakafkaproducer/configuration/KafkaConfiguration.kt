package no.nav.amt.tools.arenakafkaproducer.configuration

import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class KafkaConfiguration {

	@Bean
	open fun kafkaProducer(): KafkaProducerClientImpl<String, String> {

		val properties = KafkaPropertiesBuilder.producerBuilder()
			.withBrokerUrl(("localhost:9092"))
			.withBaseProperties()
			.withProducerId("amt-tiltak-producer")
			.withSerializers(StringSerializer::class.java, StringSerializer::class.java)
			.build()

		return KafkaProducerClientImpl<String, String>(properties)
	}

}
