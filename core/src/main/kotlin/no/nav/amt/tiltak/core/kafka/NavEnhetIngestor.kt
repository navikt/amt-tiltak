package no.nav.amt.tiltak.core.kafka

interface NavEnhetIngestor {
	fun ingestKafkaRecord(recordValue: String)
}
