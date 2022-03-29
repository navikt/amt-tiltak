package no.nav.amt.tiltak.deltaker.dbo

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val godkjentAvNavIdent: String?,
	val aktiv: Boolean, // false hvis man sletter, kommer med ny endring, kanskje hvis en endring kommer fra arena etterpå?
	val opprettetAv: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
)
