package no.nav.amt.tiltak.deltaker.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val aktiv: Boolean,
	val godkjent: Boolean,
	val arkivert: Boolean,
	val opprettetAv: UUID,
	val createdAt: LocalDateTime,
)
