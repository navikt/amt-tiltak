package no.nav.amt.tiltak.core.kafka

interface EndringPaaBrukerIngestor {
	fun ingestKafkaRecord(recordValue: String)
}
