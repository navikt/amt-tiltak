package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.util.*

data class VeilederDto(
	val id: UUID,
	val ansattId: UUID,
	val deltakerId: UUID,
	val erMedveileder: Boolean,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
)
