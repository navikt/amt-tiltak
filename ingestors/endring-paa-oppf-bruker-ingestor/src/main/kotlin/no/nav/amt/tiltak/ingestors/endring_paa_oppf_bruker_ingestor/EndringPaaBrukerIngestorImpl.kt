package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

import no.nav.amt.tiltak.clients.norg.NorgClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.core.domain.tiltak.NavKontor
import no.nav.amt.tiltak.core.port.BrukerService
import org.springframework.stereotype.Service

@Service
class EndringPaaBrukerIngestorImpl(
	private val brukerService: BrukerService,
	private val norgClient: NorgClient
) : EndringPaaBrukerIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val brukerRecord = JsonUtils.fromJson(recordValue, EndringPaaBrukerRecord::class.java)
		val bruker = brukerService.getBruker(brukerRecord.fodselsnummer) ?: return

		if (bruker.navKontor?.enhetId == brukerRecord.oppfolgingsenhet) return
		if (brukerRecord.oppfolgingsenhet == null) return

		val enhetNavn = norgClient.hentNavKontorNavn(brukerRecord.oppfolgingsenhet)
		val navKontor = NavKontor(brukerRecord.oppfolgingsenhet, enhetNavn)

		brukerService.oppdaterNavKontor(bruker.fodselsnummer, navKontor)

	}
}
