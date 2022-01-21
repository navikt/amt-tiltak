package no.nav.amt.tiltak.ingestors.tildelt_veileder_ingestor

interface TildeltVeilederIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
