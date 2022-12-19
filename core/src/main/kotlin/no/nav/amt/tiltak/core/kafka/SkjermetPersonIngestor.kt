package no.nav.amt.tiltak.core.kafka

interface SkjermetPersonIngestor {
	fun ingest(recordKey: String, recordValue: String)
}
