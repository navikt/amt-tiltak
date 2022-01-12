package no.nav.amt.tiltak.ingestors.arena_acl_ingestor

interface ArenaAclIngestor {

	fun ingestKafkaMessageValue(messageValue: String)

}
