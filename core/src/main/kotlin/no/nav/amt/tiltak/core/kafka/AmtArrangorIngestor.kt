package no.nav.amt.tiltak.core.kafka

interface AmtArrangorIngestor {
	fun ingestArrangor(recordValue: String?)
}
