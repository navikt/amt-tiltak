package no.nav.amt.tiltak.endringsmelding

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val godkjentAvNavAnsatt: UUID?,
	val godkjentTidspunkt: LocalDateTime?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val opprettetAvId: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)
