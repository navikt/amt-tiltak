package no.nav.amt.tiltak.core.kafka

interface AnsattIngestor {
	fun ingestAnsatt(recordValue: String?)
}
