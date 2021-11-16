package no.nav.amt.tools.arenakafkaproducer.producers

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.format.DateTimeFormatter

abstract class Producer<T, W>(
	private val kafkaProducer: KafkaProducerClientImpl<String, String>,
	private val topic: String
) {

	val objectMapper = jacksonObjectMapper()
		.configure(MapperFeature.USE_STD_BEAN_NAMING, true)

	val operationTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")

	fun run() {
		send(topic, readFile())
	}

	abstract fun wrap(entry: T): W
	abstract fun readFile(): List<T>

	private fun send(topic: String, data: List<T>) {
		data.forEach { entry ->
			kafkaProducer.send(
				ProducerRecord(
					topic,
					objectMapper.writeValueAsString(wrap(entry))
				)
			)
		}
	}
}
