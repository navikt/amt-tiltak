package no.nav.amt.tiltak.core.kafka

interface NavAnsattIngestor {
	fun ingest(value: String)
}
