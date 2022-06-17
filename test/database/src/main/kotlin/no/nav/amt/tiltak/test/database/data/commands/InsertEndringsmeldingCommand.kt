package no.nav.amt.tiltak.test.database.data.commands

import java.time.LocalDate
import java.util.*

data class InsertEndringsmeldingCommand(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate,
	val aktiv: Boolean,
	val opprettetAvArrangorAnsattId: UUID,
)
