package no.nav.amt.tiltak.core.kafka

interface GjennomforingIngestor {

	fun ingestKafkaRecord(gjennomforingId: String, recordValue: String?)

}
