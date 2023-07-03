package no.nav.amt.tiltak.kafka.arrangor_ingestor

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.kafka.AmtArrangorIngestor
import no.nav.amt.tiltak.core.port.ArrangorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ArrangorIngestorImpl(
	private val arrangorService: ArrangorService,
	private val amtArrangorClient: AmtArrangorClient
) : AmtArrangorIngestor {

	private val logger = LoggerFactory.getLogger(javaClass)

	override fun ingestArrangor(recordValue: String?) {
		if (recordValue != null) {
			val arrangorDto = fromJsonString<ArrangorDto>(recordValue)
			if (arrangorDto.source != null) {
				logger.info("Oppdaterer arrangør med id ${arrangorDto.id}")
				if (arrangorDto.overordnetArrangorId != null) {
					amtArrangorClient.hentArrangor(arrangorDto.organisasjonsnummer)?.let {
						arrangorService.upsertArrangor(
							Arrangor(
								id = it.id,
								navn = it.navn,
								organisasjonsnummer = it.organisasjonsnummer,
								overordnetEnhetOrganisasjonsnummer = it.overordnetArrangor?.organisasjonsnummer,
								overordnetEnhetNavn = it.overordnetArrangor?.navn
							)
						)

					}
				} else {
					arrangorService.upsertArrangor(
						Arrangor(
							id = arrangorDto.id,
							navn = arrangorDto.navn,
							organisasjonsnummer = arrangorDto.organisasjonsnummer,
							overordnetEnhetNavn = null,
							overordnetEnhetOrganisasjonsnummer = null
						)
					)
				}
			}
		}
	}

	data class ArrangorDto(
		val id: UUID,
		val source: String?,
		val navn: String,
		val organisasjonsnummer: String,
		val overordnetArrangorId: UUID?
	)
}
