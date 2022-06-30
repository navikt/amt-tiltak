package no.nav.amt.tiltak.nav_enhet_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.kafka.NavEnhetIngestor
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.springframework.stereotype.Service

@Service
class NavEnhetIngestorImpl(
	private val navEnhetService: NavEnhetService
) : NavEnhetIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val navEnhet = fromJsonString<NavEnhetKafkaDto>(recordValue)

		navEnhetService.upsertNavEnhet(navEnhet.enhetId, navEnhet.navn)
	}

}
