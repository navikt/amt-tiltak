package no.nav.amt.tiltak.core.domain.tiltak

data class Mal(
	val visningstekst: String,
	val type: String,
	val valgt: Boolean,
	val beskrivelse: String?,
)