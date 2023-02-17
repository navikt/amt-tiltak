package no.nav.amt.tiltak.core.kafka

interface AktorV2Ingestor {
	fun ingestKafkaRecord(key: String, value: ByteArray?)
}
