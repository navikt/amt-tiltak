package no.nav.amt.tools.arenakafkaproducer

import no.nav.amt.tools.arenakafkaproducer.producers.TiltakDeltakerProducer
import no.nav.amt.tools.arenakafkaproducer.producers.TiltakGjennomforingProducer
import no.nav.amt.tools.arenakafkaproducer.producers.TiltakProducer
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * For Testing
 */
@SpringBootApplication
open class ArenaKafkaProducerApplication(
	private val tiltakProducer: TiltakProducer,
	private val tiltakDeltakerProducer: TiltakDeltakerProducer,
	private val tiltakGjennomforingProducer: TiltakGjennomforingProducer
) : CommandLineRunner {

	override fun run(vararg args: String?) {
		tiltakProducer.run()
		tiltakDeltakerProducer.run()
		tiltakGjennomforingProducer.run()
	}

}

fun main(args: Array<String>) {
	runApplication<ArenaKafkaProducerApplication>(*args)
}
