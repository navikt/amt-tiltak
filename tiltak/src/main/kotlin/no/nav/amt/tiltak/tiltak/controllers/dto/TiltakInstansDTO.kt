package no.nav.amt.tiltak.tiltak.controllers.dto

import java.time.LocalDateTime
import java.util.*

data class TiltakInstansDTO(
	val id: UUID,
	val navn: String,
	val oppstartsdato: LocalDateTime,
	val sluttdato: LocalDateTime,

	val tiltak: TiltakDTO
)
