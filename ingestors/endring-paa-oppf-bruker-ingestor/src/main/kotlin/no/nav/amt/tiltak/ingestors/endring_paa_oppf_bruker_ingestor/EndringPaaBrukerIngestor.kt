package no.nav.amt.tiltak.ingestors.endring_paa_oppf_bruker_ingestor

interface EndringPaaBrukerIngestor {
	fun ingestKafkaRecord(recordValue: String)
}
