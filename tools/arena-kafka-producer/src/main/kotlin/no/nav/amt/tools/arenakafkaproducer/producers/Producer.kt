package no.nav.amt.tools.arenakafkaproducer.producers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tools.arenakafkaproducer.domain.dto.ArenaTiltak
import no.nav.common.kafka.producer.KafkaProducerClientImpl
import org.apache.kafka.clients.producer.ProducerRecord
import java.io.File
import java.io.FileInputStream

abstract class Producer<T, W>(
	private val kafkaProducer: KafkaProducerClientImpl<String, String>,
	private val topic: String
) {

	val objectMapper = jacksonObjectMapper()

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
