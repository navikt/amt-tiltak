package no.nav.amt.tiltak.nav_enhet_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.kafka.NavEnhetIngestor
import no.nav.amt.tiltak.core.port.NavKontorService
import org.springframework.stereotype.Service

@Service
class NavEnhetIngestorImpl(
	private val navKontorService: NavKontorService
) : NavEnhetIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val navEnhet = fromJsonString<NavEnhetKafkaDto>(recordValue)

		navKontorService.upsertNavKontor(navEnhet.enhetId, navEnhet.navn)
	}

}
