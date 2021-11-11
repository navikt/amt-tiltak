package no.nav.amt.tiltak.ingestors.arena.domain

enum class IngestStatus {
    NEW,
    INGESTED,
    RETRY,
    FAILED,
	IGNORED
}
