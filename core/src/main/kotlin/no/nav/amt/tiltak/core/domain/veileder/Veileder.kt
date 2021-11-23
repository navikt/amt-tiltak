package no.nav.amt.tiltak.core.domain.veileder

data class Veileder(
	val navIdent: String,

	val visningsNavn: String?,
	val fornavn: String?,
	val etternavn: String?,

	val epost: String?,
)
