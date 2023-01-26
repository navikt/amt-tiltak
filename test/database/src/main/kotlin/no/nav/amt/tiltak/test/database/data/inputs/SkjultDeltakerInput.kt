package no.nav.amt.tiltak.test.database.data.inputs

import java.time.ZonedDateTime
import java.util.*

data class SkjultDeltakerInput(
	val id: UUID,
	val deltakerId: UUID,
	val skjultAvArrangorAnsattId: UUID,
	val skjultTil: ZonedDateTime,
)
