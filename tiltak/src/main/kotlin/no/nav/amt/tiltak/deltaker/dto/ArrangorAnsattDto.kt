package no.nav.amt.tiltak.deltaker.dto

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt

data class ArrangorAnsattDto (
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String
)

fun Ansatt.toDto() = ArrangorAnsattDto (
	fornavn = fornavn,
	mellomnavn = mellomnavn,
	etternavn = etternavn,
)
