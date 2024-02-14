package no.nav.amt.tiltak.core.domain.tiltak

data class DeltakelsesInnhold(
	val ledetekst: String,
	val innhold: List<Innhold>,
)

data class Innhold(
	val visningstekst: String,
	val type: String,
	val beskrivelse: String?,
)
