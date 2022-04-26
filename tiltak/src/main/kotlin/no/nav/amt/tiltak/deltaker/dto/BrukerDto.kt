package no.nav.amt.tiltak.deltaker.dto

import no.nav.amt.tiltak.core.domain.tiltak.Bruker


data class BrukerDto (
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val fodselsnummer: String,
)

fun Bruker.toDto() = BrukerDto(
	fornavn = fornavn,
	mellomnavn = mellomnavn,
	etternavn = etternavn,
	fodselsnummer = fodselsnummer,
)
