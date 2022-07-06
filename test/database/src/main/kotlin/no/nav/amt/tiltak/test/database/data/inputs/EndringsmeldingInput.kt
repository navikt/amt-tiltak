package no.nav.amt.tiltak.test.database.data.inputs

import java.time.LocalDate
import java.util.*

data class EndringsmeldingInput(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate,
	val aktiv: Boolean,
	val opprettetAvArrangorAnsattId: UUID,
)
