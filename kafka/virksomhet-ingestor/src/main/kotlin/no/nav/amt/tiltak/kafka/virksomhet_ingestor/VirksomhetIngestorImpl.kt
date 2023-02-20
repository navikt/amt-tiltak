package no.nav.amt.tiltak.kafka.virksomhet_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import no.nav.amt.tiltak.core.kafka.VirksomhetIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import org.springframework.stereotype.Component

@Component
class VirksomhetIngestorImpl(
	private val arrangorService: ArrangorService,
) : VirksomhetIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val virksomhetDto = fromJsonString<VirksomhetDto>(recordValue)

		arrangorService.oppdaterArrangor(
			ArrangorUpdate(
				navn = virksomhetDto.navn,
				organisasjonsnummer = virksomhetDto.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = virksomhetDto.overordnetEnhetOrganisasjonsnummer,
			)
		)
	}
}
