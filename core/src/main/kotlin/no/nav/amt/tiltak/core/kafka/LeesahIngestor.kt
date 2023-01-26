package no.nav.amt.tiltak.core.kafka

interface LeesahIngestor {
	fun ingestKafkaRecord(key: String, value: ByteArray)
}
