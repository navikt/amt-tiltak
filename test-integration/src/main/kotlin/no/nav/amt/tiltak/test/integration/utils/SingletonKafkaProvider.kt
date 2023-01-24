package no.nav.amt.tiltak.test.integration.utils

import no.nav.amt.tiltak.kafka.config.KafkaProperties
import no.nav.common.kafka.util.KafkaPropertiesBuilder
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.slf4j.LoggerFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

object SingletonKafkaProvider {

	private const val producerId = "INTEGRATION_PRODUCER"
	private const val consumerId = "INTEGRATION_CONSUMER"

	private val log = LoggerFactory.getLogger(javaClass)
	private val kafkaDockerImageName = getKafkaImage()

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
					.withSerializers(ByteArraySerializer::class.java, ByteArraySerializer::class.java)
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

	private fun getKafkaImage(): String {
		val tag = when (System.getProperty("os.arch")) {
			"aarch64" -> "7.2.2-1-ubi8.arm64"
			else -> "7.2.2"
		}

		return "confluentinc/cp-kafka:$tag"
	}

}
