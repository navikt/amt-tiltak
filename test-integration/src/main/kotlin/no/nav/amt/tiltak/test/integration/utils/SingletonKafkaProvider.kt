package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

object SingletonKafkaProvider {

	private const val producerId = "INTEGRATION_PRODUCER"
	private const val consumerId = "INTEGRATION_CONSUMER"

	private val log = LoggerFactory.getLogger(javaClass)
	private const val kafkaDockerImageName = "confluentinc/cp-kafka:6.2.1"

	private var kafkaContainer: KafkaContainer? = null

	fun getKafkaProperties(): KafkaProperties {
		val host = getHost()

		val properties = object : KafkaProperties {
			override fun consumer(): Properties {
				return KafkaPropertiesBuilder.consumerBuilder()
					.withBrokerUrl(host)
					.withBaseProperties()
					.withConsumerGroupId(consumerId)
					.withDeserializers(ByteArrayDeserializer::class.java, ByteArrayDeserializer::class.java)
					.build()
			}

			override fun producer(): Properties {
				return KafkaPropertiesBuilder.producerBuilder()
					.withBrokerUrl(host)
					.withBaseProperties()
					.withProducerId(producerId)
					.withSerializers(StringSerializer::class.java, StringSerializer::class.java)
					.build()
			}
		}

		return properties
	}

	private fun getHost(): String {
		if (kafkaContainer == null) {
			log.info("Starting new Kafka Instance...")
			kafkaContainer = KafkaContainer(DockerImageName.parse(kafkaDockerImageName))
			kafkaContainer!!.start()
			setupShutdownHook()
		}
		return kafkaContainer!!.bootstrapServers
	}

	private fun setupShutdownHook() {
		Runtime.getRuntime().addShutdownHook(Thread {
			log.info("Shutting down Kafka server...")
			kafkaContainer?.stop()
		})
	}


}
