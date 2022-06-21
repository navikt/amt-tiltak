package no.nav.amt.tiltak.test.database.data.commands

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class InsertDeltakerCommand(
	val id: UUID,
	val brukerId: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val dagerPerUke: Int,
	val prosentStilling: Float,
	val registrertDato: LocalDateTime,
	val innsokBegrunnelse: String?
)
