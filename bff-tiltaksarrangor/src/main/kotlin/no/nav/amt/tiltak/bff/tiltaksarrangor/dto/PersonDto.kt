package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.port.Diskresjonskode
import no.nav.amt.tiltak.core.port.Person

data class PersonDto(
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val telefonnummer: String?,
	val diskresjonskode: Diskresjonskode?
)

fun Person.toDto(): PersonDto {
	return PersonDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		telefonnummer = telefonnummer,
		diskresjonskode = diskresjonskode,
	)
}
