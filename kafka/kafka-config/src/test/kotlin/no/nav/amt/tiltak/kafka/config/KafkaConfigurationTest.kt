package no.nav.amt.tiltak.kafka.config

import no.nav.amt.tiltak.core.kafka.AmtArrangorIngestor
import no.nav.amt.tiltak.core.kafka.AnsattIngestor
import no.nav.amt.tiltak.core.kafka.ArenaAclIngestor
import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import no.nav.amt.tiltak.core.kafka.NavBrukerIngestor
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import no.nav.common.kafka.producer.util.ProducerUtils.toJsonProducerRecord
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger

@Testcontainers
class KafkaConfigurationTest {

	private val amtTiltakTopic = "test.amt-tiltak"
	private val sisteTiltaksgjennomforingerTopic: String = "test.siste-tiltaksgjennomforinger"
	private val deltakerTopic: String = "test.deltaker-v1"
	private val amtArrangorTopic: String = "test.arrangor-v1"
	private val amtArrangorAnsattTopic: String = "test.ansatt-v1"
	private val amtNavBrukerPersonaliaTopic: String = "test.nav-bruker-personalia-v1"

	@Container
	var kafkaContainer: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
		.waitingFor(HostPortWaitStrategy())

	private val dataSource = SingletonPostgresContainer.getDataSource()

	@Test
	fun `should ingest arena records after configuring kafka`() {
		val kafkaTopicProperties = KafkaTopicProperties(
			amtTiltakTopic = amtTiltakTopic,
			sisteTiltaksgjennomforingerTopic = sisteTiltaksgjennomforingerTopic,
			deltakerTopic = deltakerTopic,
			amtArrangorAnsattTopic = amtArrangorAnsattTopic,
			amtArrangorTopic = amtArrangorTopic,
			amtDeltakerTopic = "",
			amtDeltakerlisteTopic = "",
			amtEndringsmeldingTopic = "",
			amtNavBrukerPersonaliaTopic = amtNavBrukerPersonaliaTopic,
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
			override fun ingestKafkaRecord(recordValue: String) {
				counter.incrementAndGet()
			}
		}

		val gjennomforingIngestor = object : GjennomforingIngestor {
			override fun ingestKafkaRecord(gjennomforingId: String, recordValue: String?) {
				counter.incrementAndGet()
			}
		}

		val arrangorIngestor = object : AmtArrangorIngestor {
			override fun ingestArrangor(recordValue: String?) {
				counter.incrementAndGet()
			}
		}

		val ansattIngestor = object : AnsattIngestor {
			override fun ingestAnsatt(recordValue: String?) {
				counter.incrementAndGet()
			}
		}

		val navBrukerIngestor = object : NavBrukerIngestor {
			override fun ingest(key: String, value: String?) {
				counter.incrementAndGet()
			}
		}

		val config = KafkaConfiguration(
			kafkaTopicProperties,
			kafkaProperties,
			JdbcTemplate(dataSource),
			arenaAclIngestor,
			gjennomforingIngestor,
			arrangorIngestor,
			ansattIngestor,
			navBrukerIngestor,
		)

		config.onApplicationEvent(null)

		val kafkaProducer = KafkaProducerClientImpl<String, String>(kafkaProperties.producer())
		val value = "some value"

		kafkaProducer.sendSync(toJsonProducerRecord(amtTiltakTopic, "1", value))
		kafkaProducer.sendSync(toJsonProducerRecord(sisteTiltaksgjennomforingerTopic, "1", value))
		kafkaProducer.sendSync(toJsonProducerRecord(amtArrangorTopic, "1", value))
		kafkaProducer.sendSync(toJsonProducerRecord(amtArrangorAnsattTopic, "1", value))
		kafkaProducer.sendSync(toJsonProducerRecord(amtNavBrukerPersonaliaTopic, "1", value))

		kafkaProducer.close()

		// This test can be rewritten to retry the assertion until a given time has passed to speed things up

		Thread.sleep(3000)

		assertEquals(5, counter.get())
	}

}
