package no.nav.amt.tiltak.test.database.data.inputs

import java.time.ZonedDateTime
import java.util.*

data class ArrangorVeilederDboInput(
	val id: UUID,
	val ansattId: UUID,
	val deltakerId: UUID,
	val erMedveileder: Boolean,
	val gyldigFra: ZonedDateTime,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime = ZonedDateTime.now(),
	val modifiedAt: ZonedDateTime = ZonedDateTime.now(),
)
