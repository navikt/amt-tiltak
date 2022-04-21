package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavKontorService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EndringPaaBrukerIngestorImpl(
	private val brukerService: BrukerService,
	private val navKontorService: NavKontorService
) : EndringPaaBrukerIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaRecord(recordValue: String) {
		val brukerRecord = JsonUtils.fromJsonString<EndringPaaBrukerKafkaDto>(recordValue)
		val bruker = brukerService.getBruker(brukerRecord.fodselsnummer) ?: return

		if (bruker.navKontor?.enhetId == brukerRecord.oppfolgingsenhet) return
		if (brukerRecord.oppfolgingsenhet == null) return

		log.info("Endrer oppfølgingsenhet på bruker med id=${bruker.id}")
		val navKontor = navKontorService.getNavKontor(brukerRecord.oppfolgingsenhet)

		brukerService.oppdaterNavKontor(bruker.fodselsnummer, navKontor)

	}
}
