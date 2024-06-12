package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Adressebeskyttelse

data class DeltakerDto(
	val fornavn: String? = null,
	val mellomnavn: String? = null,
	val etternavn: String? = null,
	val fodselsnummer: String? = null,
	val erSkjermet: Boolean,
	val adressebeskyttelse: Adressebeskyttelse?,
)
