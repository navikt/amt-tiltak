package no.nav.amt.tiltak.test.database.data.commands

import java.time.LocalDateTime
import java.util.*

data class InsertDeltakerStatusCommand(
	val id: UUID,
	val deltaker_id: UUID,
	val gyldigFra: LocalDateTime,
	val status: String,
	val aktiv: Boolean
)
