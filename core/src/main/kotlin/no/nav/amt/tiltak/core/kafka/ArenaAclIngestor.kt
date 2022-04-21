package no.nav.amt.tiltak.core.kafka

interface ArenaAclIngestor {

	fun ingestKafkaRecord(recordValue: String)

}
