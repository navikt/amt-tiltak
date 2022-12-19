package no.nav.amt.tiltak.kafka.tiltaksgjennomforing_ingestor

import no.nav.amt.tiltak.core.kafka.GjennomforingIngestor
import org.springframework.stereotype.Service

@Service
class GjennomforingIngestorImpl: GjennomforingIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		TODO("Not yet implemented")
	}
}
