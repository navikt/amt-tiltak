package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import no.nav.amt.tiltak.core.port.*
import no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.dto.SisteTildeltVeilederV1RecordValue
import no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.utils.JsonUtils.getObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TildeltVeilederIngestorImpl(
	private val veilederConnector: VeilederConnector,
	private val veilederService: VeilederService,
	private val personService: PersonService,
	private val brukerService: BrukerService
) : TildeltVeilederIngestor {

	private val log = LoggerFactory.getLogger(TildeltVeilederIngestorImpl::class.java)

	override fun ingestKafkaRecord(recordValue: String) {
		val sisteTildeltVeileder = getObjectMapper().readValue(recordValue, SisteTildeltVeilederV1RecordValue::class.java)

		val gjeldendeIdent = personService.hentGjeldendePersonligIdent(sisteTildeltVeileder.aktorId)

		if (!brukerService.finnesBruker(gjeldendeIdent)) {
			log.info("Tildelt veileder endret. Bruker finnes ikke, hopper over kafka melding")
			return
		}

		val veileder = veilederConnector.hentVeileder(sisteTildeltVeileder.veilederId)
			?: throw IllegalStateException("Klarte ikke å hente informasjon om veileder med ident ${sisteTildeltVeileder.veilederId}")

		val veilederId = veilederService.upsertVeileder(veileder)

		brukerService.oppdaterAnsvarligVeileder(gjeldendeIdent, veilederId)
	}

}
