package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.core.kafka.KafkaProducerService
import no.nav.amt.tiltak.core.kafka.NavEnhetKafkaDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class PubliserNavEnhetService(
	private val kafkaProducerService: KafkaProducerService,
	private val norgClient: NorgClient
) {

	private val log = LoggerFactory.getLogger(javaClass)

	open fun publiserAlleNavEnheter() {
		val alleEnheter = norgClient.hentAlleNavEnheter()

		log.info("Publiserer ${alleEnheter.size} NAV enheter på kafka")

		alleEnheter.forEach {
			kafkaProducerService.sendNavEnhet(
				NavEnhetKafkaDto(
					enhetId = it.enhetId,
					navn = it.navn
				)
			)
		}
	}

}
