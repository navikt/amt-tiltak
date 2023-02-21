package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.util.*


data class TilgjengeligVeilederDto(
	val ansattId: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
)
