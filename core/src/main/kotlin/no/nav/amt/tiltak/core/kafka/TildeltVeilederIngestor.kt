package no.nav.amt.tiltak.core.kafka

interface TildeltVeilederIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
