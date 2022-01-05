package no.nav.amt.tiltak.ingestors.arena.domain

internal enum class IngestStatus {
    NEW,
    INGESTED,
    RETRY,
    FAILED,
	IGNORED
}
