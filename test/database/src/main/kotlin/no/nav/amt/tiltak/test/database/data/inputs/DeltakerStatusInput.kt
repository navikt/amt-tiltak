package no.nav.amt.tiltak.test.database.data.inputs

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

data class DeltakerStatusInput(
	val id: UUID,
	val deltakerId: UUID,
	val gyldigFra: LocalDateTime,
	val status: String,
	val aarsak: String?,
	val aktiv: Boolean,
	val createdAt: ZonedDateTime = ZonedDateTime.of(2022, 2, 13, 0, 0, 0, 0, ZoneId.systemDefault())
)
