package no.nav.amt.tiltak.test.database.data.commands

import java.time.LocalDate
import java.util.*

data class InsertGjennomforingCommand(
	val id: UUID,
	val tiltak_id: UUID,
	val arrangor_id: UUID,
	val navn: String,
	val status: String,
	val start_dato: LocalDate,
	val slutt_dato: LocalDate,
	val registrert_dato: LocalDate,
	val fremmote_dato: LocalDate
)
