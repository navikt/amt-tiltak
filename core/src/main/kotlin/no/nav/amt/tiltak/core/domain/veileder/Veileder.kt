package no.nav.amt.tiltak.core.domain.veileder

data class Veileder(
	val navIdent: String,

	val fornavn: String,
	val mellomnavn: String,
	val etternavn: String,

	val telefon: String,
	val epost: String,
)
