package no.nav.amt.tiltak.bff.nav_ansatt.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val bruker: BrukerDto,
	val startDato: LocalDate?,
	val aktiv: Boolean,
	val godkjent: Boolean,
	val arkivert: Boolean,
	val opprettetAvArrangorAnsatt: ArrangorAnsattDto,
	val opprettetDato: LocalDateTime,
)
