package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor

class ArrangorDto (
	val virksomhetNavn: String,
	val organisasjonNavn: String?,
	val virksomhetOrgnr: String,
)

fun Arrangor.toDto() = ArrangorDto (
	virksomhetNavn = navn,
	organisasjonNavn = overordnetEnhetNavn,
	virksomhetOrgnr = organisasjonsnummer,
)
