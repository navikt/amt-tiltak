package no.nav.amt.tiltak.ingestors.arena.exceptions

class DependencyNotIngestedException(
	m: String,
	exception: Exception? = null
) : Exception(m, exception)
