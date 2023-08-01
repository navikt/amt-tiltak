package no.nav.amt.tiltak.core.kafka

interface NavBrukerIngestor {
	fun ingest(key: String, value: String?)
}
