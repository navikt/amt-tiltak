package no.nav.amt.tiltak.deltaker.dbo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerUpdateDbo(
	val id: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
)
