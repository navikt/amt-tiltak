package no.nav.amt.tiltak.kafka.virksomheter_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorUpdate
import no.nav.amt.tiltak.core.kafka.VirksomheterIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import org.springframework.stereotype.Component

@Component
class VirksomheterIngestorImpl(
	private val arrangorService: ArrangorService,
) : VirksomheterIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val virksomhetDto = fromJsonString<VirksomhetDto>(recordValue)

		if (!skalOppdateres(virksomhetDto)) return

		arrangorService.oppdaterArrangor(
			ArrangorUpdate(
				navn = virksomhetDto.navn,
				organisasjonsnummer = virksomhetDto.organisasjonsnummer,
				overordnetEnhetOrganisasjonsnummer = virksomhetDto.overordnetEnhetOrganisasjonsnummer,
			)
		)
	}

	private fun skalOppdateres(virksomhetDto: VirksomhetDto): Boolean {
		val arrangor = arrangorService.getArrangorByVirksomhetsnummer(virksomhetDto.organisasjonsnummer)

		if (arrangor == null ||
			(virksomhetDto.navn == arrangor.navn &&
			virksomhetDto.overordnetEnhetOrganisasjonsnummer == arrangor.overordnetEnhetOrganisasjonsnummer)
		) return false

		return true
	}
}
