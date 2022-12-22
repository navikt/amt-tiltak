package no.nav.amt.tiltak.ingestors.skjermet_person_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.kafka.SkjermetPersonIngestor
import no.nav.amt.tiltak.core.port.DeltakerService
import org.springframework.stereotype.Service

@Service
class SkjermetPersonIngestorImpl(
	private val deltakerService: DeltakerService
) : SkjermetPersonIngestor {

	override fun ingest(recordKey: String, recordValue: String) {
		val erSkjermet = JsonUtils.fromJsonString<Boolean>(recordValue)

		deltakerService.hentDeltakereMedPersonIdent(recordKey).firstOrNull() ?: return

		deltakerService.settSkjermet(recordKey, erSkjermet)

	}
}
