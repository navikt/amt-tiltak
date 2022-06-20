package no.nav.amt.tiltak.deltaker.dbo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerInsertDbo(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
	val registrertDato: LocalDateTime,
	val begrunnelseForDeltakelse: String? = null
)
