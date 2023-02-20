package no.nav.amt.tiltak.core.kafka

interface VirksomhetIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
