package no.nav.amt.tiltak.test.database.data.commands

import java.time.ZonedDateTime
import java.util.*

data class InsertDeltakerStatusCommand(
	val id: UUID,
	val deltaker_id: UUID,
	val endret_dato: ZonedDateTime,
	val status: String,
	val aktiv: Boolean
)
