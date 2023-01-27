package no.nav.amt.tiltak.bff.nav_ansatt.dto

data class ArrangorDto(
	val virksomhetNavn: String,
	val virksomhetOrgnr: String,
	val organisasjonNavn: String?,
	val organisasjonOrgnr: String?
)
