package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.kafka.EndringPaaBrukerIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class EndringPaaBrukerIngestorImpl(
	private val brukerService: BrukerService,
	private val norgClient: NorgClient
) : EndringPaaBrukerIngestor {

	private val log = LoggerFactory.getLogger(javaClass)

	override fun ingestKafkaRecord(recordValue: String) {
		val brukerRecord = JsonUtils.fromJsonString<EndringPaaBrukerKafkaDto>(recordValue)
		val bruker = brukerService.getBruker(brukerRecord.fodselsnummer) ?: return

		if (bruker.navKontor?.enhetId == brukerRecord.oppfolgingsenhet) return
		if (brukerRecord.oppfolgingsenhet == null) return

		log.info("Endrer oppfølgingsenhet på bruker med id=${bruker.id}")
		val enhetNavn = norgClient.hentNavKontorNavn(brukerRecord.oppfolgingsenhet)
		val navKontor = NavKontor(UUID.randomUUID(), brukerRecord.oppfolgingsenhet, enhetNavn)

		brukerService.oppdaterNavKontor(bruker.fodselsnummer, navKontor)

	}
}
