package no.nav.amt.tiltak.core.kafka

interface VirksomheterIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
