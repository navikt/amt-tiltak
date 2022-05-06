package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import no.nav.amt.tiltak.common.json.JsonUtils.fromJsonString
import no.nav.amt.tiltak.core.kafka.TildeltVeilederIngestor
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.VeilederConnector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TildeltVeilederIngestorImpl(
    private val veilederConnector: VeilederConnector,
    private val navAnsattService: NavAnsattService,
    private val personService: PersonService,
    private val brukerService: BrukerService
) : TildeltVeilederIngestor {

	private val log = LoggerFactory.getLogger(TildeltVeilederIngestorImpl::class.java)

	override fun ingestKafkaRecord(recordValue: String) {
		val sisteTildeltVeileder = fromJsonString<SisteTildeltVeilederDto>(recordValue)

		val gjeldendeIdent = personService.hentGjeldendePersonligIdent(sisteTildeltVeileder.aktorId)

		if (!brukerService.finnesBruker(gjeldendeIdent)) {
			log.info("Tildelt veileder endret. Bruker finnes ikke, hopper over kafka melding")
			return
		}

		val veileder = veilederConnector.hentVeileder(sisteTildeltVeileder.veilederId)
			?: throw IllegalStateException("Klarte ikke å hente informasjon om veileder med ident ${sisteTildeltVeileder.veilederId}")

		val veilederId = navAnsattService.upsertVeileder(veileder)

		brukerService.oppdaterAnsvarligVeileder(gjeldendeIdent, veilederId)
	}

}
