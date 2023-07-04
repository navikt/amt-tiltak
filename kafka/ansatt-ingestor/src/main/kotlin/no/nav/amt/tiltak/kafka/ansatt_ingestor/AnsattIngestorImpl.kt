package no.nav.amt.tiltak.kafka.ansatt_ingestor

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.arrangor.ArrangorAnsatt
import no.nav.amt.tiltak.core.kafka.AnsattIngestor
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.amt.tiltak.core.port.ArrangorService
import no.nav.amt.tiltak.kafka.ansatt_ingestor.model.AnsattDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AnsattIngestorImpl(
	private val arrangorService: ArrangorService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	private val amtArrangorClient: AmtArrangorClient
) : AnsattIngestor {

	private val logger = LoggerFactory.getLogger(javaClass)

	override fun ingestAnsatt(recordValue: String?) {
		if (recordValue != null) {
			val ansattDto = fromJsonString<AnsattDto>(recordValue)
			if (ansattDto.source != null) {
				logger.info("Oppdaterer ansatt med id ${ansattDto.id}")
				opprettArrangorerSomMangler(ansattDto)
				arrangorAnsattTilgangService.oppdaterRollerOgTilgangerForAnsatt(ansattDto.toArrangorAnsatt())
			}
		}
	}

	private fun opprettArrangorerSomMangler(ansattDto: AnsattDto) {
		val arrangorerSomMangler = arrangorService.getArrangorerSomMangler(ansattDto.arrangorer.map { it.arrangorId })

		arrangorerSomMangler.forEach { arrangorId ->
			val tilknyttetArrangor = ansattDto.arrangorer.find { it.arrangorId == arrangorId }
				?: throw IllegalStateException("Fant ikke arrangør for ansatt i liste, skal ikke kunne skje")
			if (tilknyttetArrangor.arrangor != null) {
				arrangorService.upsertArrangor(Arrangor(
					id = arrangorId,
					navn = tilknyttetArrangor.arrangor.navn,
					organisasjonsnummer = tilknyttetArrangor.arrangor.organisasjonsnummer,
					overordnetEnhetOrganisasjonsnummer = tilknyttetArrangor.overordnetArrangor?.organisasjonsnummer,
					overordnetEnhetNavn = tilknyttetArrangor.overordnetArrangor?.navn
				))
			} else {
				val arrangor = amtArrangorClient.hentArrangor(arrangorId) ?: throw IllegalStateException("Arrangør med id $arrangorId finnes ikke, kan ikke lagre ansatt")
				arrangorService.upsertArrangor(
					Arrangor(
						id = arrangorId,
						navn = arrangor.navn,
						organisasjonsnummer = arrangor.organisasjonsnummer,
						overordnetEnhetOrganisasjonsnummer = arrangor.overordnetArrangor?.organisasjonsnummer,
						overordnetEnhetNavn = arrangor.overordnetArrangor?.navn
				))
			}
		}
		logger.info("Opprettet ${arrangorerSomMangler.size} arrangører")
	}

	private fun AnsattDto.toArrangorAnsatt(): ArrangorAnsatt {
		return ArrangorAnsatt(
			id = id,
			personalia = personalia,
			arrangorer = arrangorer.map {
				ArrangorAnsatt.TilknyttetArrangorDto(
					arrangorId = it.arrangorId,
					arrangor = it.arrangor?.let { arrangor ->
						ArrangorAnsatt.Arrangor(
							id = arrangor.id,
							navn = arrangor.navn,
							organisasjonsnummer = arrangor.organisasjonsnummer
						)
					} ?: arrangorService.getArrangorById(it.arrangorId).toArrangorAnsattArrangor(),
					overordnetArrangor = it.overordnetArrangor?.let { overordnetArrangor ->
						ArrangorAnsatt.Arrangor(
							id = overordnetArrangor.id,
							navn = overordnetArrangor.navn,
							organisasjonsnummer = overordnetArrangor.organisasjonsnummer
						)
					},
					roller = it.roller,
					veileder = it.veileder,
					koordinator = it.koordinator
				)
			}
		)
	}

	private fun Arrangor.toArrangorAnsattArrangor(): ArrangorAnsatt.Arrangor {
		return ArrangorAnsatt.Arrangor(
			id = id,
			navn = navn,
			organisasjonsnummer = organisasjonsnummer
		)
	}
}
