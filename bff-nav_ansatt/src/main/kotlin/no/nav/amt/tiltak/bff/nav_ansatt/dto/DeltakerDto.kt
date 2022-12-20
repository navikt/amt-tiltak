package no.nav.amt.tiltak.bff.nav_ansatt.dto

data class DeltakerDto(
	val fornavn: String? = null,
	val mellomnavn: String? = null,
	val etternavn: String? = null,
	val fodselsnummer: String? = null,
	val erSkjermet: Boolean
)
