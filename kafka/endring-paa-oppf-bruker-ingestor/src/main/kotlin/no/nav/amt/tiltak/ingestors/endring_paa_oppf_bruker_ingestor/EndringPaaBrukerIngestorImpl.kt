package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.NavEnhetService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EndringPaaBrukerIngestorImpl(
	private val deltakerService: DeltakerService,
	private val navEnhetService: NavEnhetService
) : EndringPaaBrukerIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaRecord(recordValue: String) {
		val brukerRecord = JsonUtils.fromJsonString<EndringPaaBrukerKafkaDto>(recordValue)
		val deltaker = deltakerService.hentDeltakereMedFnr(brukerRecord.fodselsnummer).firstOrNull() ?: return
		val gammelNavEnhet = deltaker.navEnhetId?.let { navEnhetService.getNavEnhet(it) }

		if (gammelNavEnhet?.enhetId == brukerRecord.oppfolgingsenhet) return
		if (brukerRecord.oppfolgingsenhet == null) return

		log.info("Endrer oppfølgingsenhet på bruker tilknyttet deltaker med id=${deltaker.id}")
		val navEnhet = navEnhetService.getNavEnhet(brukerRecord.oppfolgingsenhet)

		deltakerService.oppdaterNavEnhet(deltaker.fodselsnummer, navEnhet)
	}

}
