package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.port.Person

data class PersonDto(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
)

fun Person.toDto(): PersonDto {
	return PersonDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
	)
}
