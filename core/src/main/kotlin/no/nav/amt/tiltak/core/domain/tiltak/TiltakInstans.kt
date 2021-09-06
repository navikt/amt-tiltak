package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class TiltakInstans(
	val id: UUID,
	val navn: String,
	val oppstartsdato: LocalDateTime,
	val sluttdato: LocalDateTime,

	val tiltak: Tiltak
)
