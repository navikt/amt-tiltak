package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

import no.nav.amt.tiltak.core.port.VeilederConnector
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.dto.SisteTildeltVeilederV1RecordValue
import no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor.utils.JsonUtils.getObjectMapper
import org.springframework.stereotype.Service

@Service
class TildeltVeilederIngestorImpl(
	private val veilederConnector: VeilederConnector,
	private val veilederService: VeilederService
) : TildeltVeilederIngestor {

	override fun ingestKafkaRecord(recordValue: String) {
		val sisteTildeltVeileder = getObjectMapper().readValue(recordValue, SisteTildeltVeilederV1RecordValue::class.java)

		val veileder = veilederConnector.hentVeileder(sisteTildeltVeileder.veilederId)
			?: throw IllegalStateException("Klarte ikke å hente informasjon om veileder med ident ${sisteTildeltVeileder.veilederId}")

		val veilederId = veilederService.upsertVeileder(veileder)

		/*
			TODO:
				0. Kan vurdere å sjekke om brukeren finnes før vi upserter veileder
			 	1. Konverter aktørid til fnr
			 	2. Oppdater veilederId på bruker med fnr
		 */
	}

}
