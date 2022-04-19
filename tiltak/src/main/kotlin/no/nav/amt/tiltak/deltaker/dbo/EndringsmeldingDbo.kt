package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val godkjentAvNavAnsatt: UUID?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val opprettetAv: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {
	fun toDto() = EndringsmeldingDto(
		id = id,
		deltakerId = deltakerId,
		startDato = startDato,
		aktiv = aktiv,
		godkjent = godkjentAvNavAnsatt != null,
		arkivert = !aktiv || godkjentAvNavAnsatt != null,
		opprettetAv = opprettetAv,
		createdAt = createdAt,
	)
}
