package no.nav.amt.tiltak.configuration.kafka

import no.nav.amt.tiltak.application.configuration.kafka.KafkaConfiguration
import no.nav.amt.tiltak.application.configuration.kafka.KafkaProperties
import no.nav.amt.tiltak.application.configuration.kafka.KafkaTopicProperties
import no.nav.amt.tiltak.ingestors.arena.ArenaIngestor
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.producer.util.ProducerUtils
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*

@Testcontainers
class KafkaConfigurationTest {

	@Container
	var kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

	@Test
	fun test() {

		val kafkaTopicProperties = KafkaTopicProperties(
			arenaTiltakDeltakerTopic = "arena-tiltak",
			arenaTiltaksgjennomforingTopic = "arena-tiltaksgjennomforing",
			arenaTiltaksgruppeTopic = "arena-tiltaksgruppe",
			arenaTiltakTopic = "arena-tiltak",
			)

		val kafkaProperties = object : KafkaProperties {
			override fun consumer(): Properties {
				return KafkaPropertiesBuilder.consumerBuilder()
					.withBrokerUrl(kafkaContainer.bootstrapServers)
					.withBaseProperties()
					.withConsumerGroupId("amt-tiltak-consumer")
					.withDeserializers(ByteArrayDeserializer::class.java, ByteArrayDeserializer::class.java)
					.build()
			}

			override fun producer(): Properties {
				return KafkaPropertiesBuilder.producerBuilder()
					.withBrokerUrl(kafkaContainer.bootstrapServers)
					.withBaseProperties()
					.withProducerId("amt-tiltak-producer")
					.withSerializers(StringSerializer::class.java, StringSerializer::class.java)
					.build()
			}
		}

		val arenaIngestor = ArenaIngestor()

		KafkaConfiguration(
			kafkaTopicProperties,
			kafkaProperties,
			arenaIngestor
		)

		val kafkaProducer = KafkaProducerClientImpl<String, String>(kafkaProperties.producer())

		val record = ProducerUtils.toJsonProducerRecord("arena-tiltak", "1", "some value")

		kafkaProducer.sendSync(record)
		kafkaProducer.close()

		Thread.sleep(5000)
	}

}
