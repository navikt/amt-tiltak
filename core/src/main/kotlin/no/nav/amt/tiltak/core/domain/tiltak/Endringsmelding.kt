package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

data class Endringsmelding(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val ferdiggjortAvNavAnsattId: UUID?,
	val ferdiggjortTidspunkt: ZonedDateTime?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val opprettetAvArrangorAnsattId: UUID,
	val opprettet: LocalDateTime,
)
