package no.nav.amt.tiltak.test.database.data.commands

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class InsertDeltakerCommand(
	val id: UUID,
	val bruker_id: UUID,
	val gjennomforing_id: UUID,
	val start_dato: LocalDate,
	val slutt_dato: LocalDate,
	val dager_per_uke: Int,
	val prosent_stilling: Float,
	val registrert_dato: LocalDateTime
)
