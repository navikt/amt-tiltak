package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import java.util.UUID

data class DeltakerlisteDto(
	val id: UUID,
	val navn: String,
	val type: String,
	val erKurs: Boolean
)
