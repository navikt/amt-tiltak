package no.nav.amt.tiltak.core.kafka

interface GjennomforingIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
