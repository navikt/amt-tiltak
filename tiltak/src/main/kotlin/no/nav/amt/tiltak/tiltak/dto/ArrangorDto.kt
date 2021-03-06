package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor

class ArrangorDto (
	val virksomhetNavn: String,
	val organisasjonNavn: String?,
)

fun Arrangor.toDto() = ArrangorDto (
	virksomhetNavn = navn,
	organisasjonNavn = overordnetEnhetNavn
)
