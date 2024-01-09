package no.nav.amt.tiltak.core.kafka

interface DeltakerIngestor {
	fun ingest(key: String, value: String?)
}
