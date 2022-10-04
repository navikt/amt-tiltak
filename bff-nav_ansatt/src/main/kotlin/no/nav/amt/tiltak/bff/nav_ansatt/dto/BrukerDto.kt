package no.nav.amt.tiltak.bff.nav_ansatt.dto

data class BrukerDto(
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
)
