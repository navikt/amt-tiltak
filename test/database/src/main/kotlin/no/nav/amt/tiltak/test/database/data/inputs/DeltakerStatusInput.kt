package no.nav.amt.tiltak.test.database.data.inputs

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusInput(
	val id: UUID,
	val deltakerId: UUID,
	val gyldigFra: LocalDateTime,
	val status: String,
	val aktiv: Boolean
)
