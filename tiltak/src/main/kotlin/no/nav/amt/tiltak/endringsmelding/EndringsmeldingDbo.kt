package no.nav.amt.tiltak.endringsmelding

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val ferdiggjortAvNavAnsattId: UUID?,
	val ferdiggjortTidspunkt: ZonedDateTime?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val opprettetAvArrangorAnsattId: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)
