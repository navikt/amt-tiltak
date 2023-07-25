package no.nav.amt.tiltak.deltaker.dbo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerUpsertDbo(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String? = null
)
