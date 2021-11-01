package no.nav.amt.tiltak.configuration.kafka

import junit.framework.Assert.assertEquals
import no.nav.amt.tiltak.application.configuration.kafka.KafkaConfiguration
import no.nav.amt.tiltak.application.configuration.kafka.KafkaProperties
import no.nav.amt.tiltak.application.configuration.kafka.KafkaTopicProperties
import no.nav.amt.tiltak.core.port.ArenaIngestor
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.producer.util.ProducerUtils.toJsonProducerRecord
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Testcontainers
class KafkaConfigurationTest {

	@Container
	var kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))

	@Test
	fun `should ingest arena records after configuring kafka`() {

		val kafkaTopicProperties = KafkaTopicProperties(
			arenaTiltakTopic = "arena-tiltak",
			arenaTiltaksgjennomforingTopic = "arena-tiltaksgjennomforing",
			arenaTiltaksgruppeTopic = "arena-tiltaksgruppe",
			arenaTiltakDeltakerTopic = "arena-tiltak-deltaker",
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

		val counter = AtomicInteger()

		val arenaIngestor = object : ArenaIngestor {
			override fun ingest(data: String) {
				counter.incrementAndGet()
			}
		}

		// Creating the config will automatically start the consumer
		KafkaConfiguration(
			kafkaTopicProperties,
			kafkaProperties,
			arenaIngestor
		)

		val kafkaProducer = KafkaProducerClientImpl<String, String>(kafkaProperties.producer())

		kafkaProducer.sendSync(toJsonProducerRecord("arena-tiltak", "1", "some value"))
		kafkaProducer.sendSync(toJsonProducerRecord("arena-tiltaksgjennomforing", "1", "some value"))
		kafkaProducer.sendSync(toJsonProducerRecord("arena-tiltaksgruppe", "1", "some value"))
		kafkaProducer.sendSync(toJsonProducerRecord("arena-tiltak-deltaker", "1", "some value"))

		kafkaProducer.close()

		// This test can be rewritten to retry the assertion until a given time has passed to speed things up

		Thread.sleep(3000)

		assertEquals(4, counter.get())
	}

}
