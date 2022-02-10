package no.nav.amt.tiltak.configuration.kafka

import junit.framework.Assert.assertEquals
import no.nav.amt.tiltak.application.configuration.kafka.KafkaConfiguration
import no.nav.amt.tiltak.application.configuration.kafka.KafkaProperties
import no.nav.amt.tiltak.application.configuration.kafka.KafkaTopicProperties
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.ArenaAclIngestor
import no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.TildeltVeilederIngestor
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.producer.util.ProducerUtils.toJsonProducerRecord
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Test
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Testcontainers
class KafkaConfigurationTest {

	val amtTiltakTopic = "amt-tiltak"
	val sisteTilordnetVeilederTopic = "siste-tilordnet-veileder-v1"

	@Container
	var kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
		.waitingFor(HostPortWaitStrategy())

	val dataSource = SingletonPostgresContainer.getDataSource()

	@Test
	fun `should ingest arena records after configuring kafka`() {
		val kafkaTopicProperties = KafkaTopicProperties(
			amtTiltakTopic = amtTiltakTopic,
			sisteTilordnetVeilederTopic = sisteTilordnetVeilederTopic
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

		val arenaAclIngestor = object : ArenaAclIngestor {
			override fun ingestKafkaMessageValue(messageValue: String) {
				counter.incrementAndGet()
			}
		}

		val tildeltVeilederIngestor = object : TildeltVeilederIngestor {
			override fun ingestKafkaRecord(recordValue: String) {
				counter.incrementAndGet()
			}
		}

		val config = KafkaConfiguration(
			kafkaTopicProperties,
			kafkaProperties,
			JdbcTemplate(dataSource),
			arenaAclIngestor,
			tildeltVeilederIngestor,
		)

		config.onApplicationEvent(null)

		val kafkaProducer = KafkaProducerClientImpl<String, String>(kafkaProperties.producer())
		val value = "some value"

		kafkaProducer.sendSync(toJsonProducerRecord(amtTiltakTopic, "1", value))
		kafkaProducer.sendSync(toJsonProducerRecord(sisteTilordnetVeilederTopic, "1", value))

		kafkaProducer.close()

		// This test can be rewritten to retry the assertion until a given time has passed to speed things up

		Thread.sleep(3000)

		assertEquals(2, counter.get())
	}

}
