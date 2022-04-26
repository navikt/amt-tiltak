package no.nav.amt.tiltak.deltaker.dto

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
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

fun Endringsmelding.toDto() = EndringsmeldingDto(
	id = id,
	bruker = bruker.toDto(),
	startDato = startDato,
	aktiv = aktiv,
	godkjent = godkjent,
	arkivert = arkivert,
	opprettetAvArrangorAnsatt = opprettetAvArrangorAnsatt.toDto(),
	opprettetDato = opprettetDato
)
