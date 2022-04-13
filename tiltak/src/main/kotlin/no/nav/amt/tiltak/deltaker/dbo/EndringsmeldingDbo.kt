package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.deltaker.dto.toDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val godkjentAvNavAnsatt: UUID?,
	val aktiv: Boolean, // false hvis man sletter eller kommer en ny endring
	val opprettetAvId: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {
	fun toDto(bruker: Bruker, opprettetAv: Ansatt) = EndringsmeldingDto(
		id = id,
		bruker = bruker.toDto(),
		startDato = startDato,
		aktiv = aktiv,
		godkjent = godkjentAvNavAnsatt != null,
		arkivert = !aktiv || godkjentAvNavAnsatt != null,
		opprettetAvArrangorAnsatt = opprettetAv.toDto(),
		opprettetDato = createdAt,
	)

	fun toEndringsmelding(bruker: Bruker, opprettetAv: Ansatt) = Endringsmelding(
		id = id,
		bruker = bruker,
		startDato = startDato,
		aktiv = aktiv,
		godkjent = godkjentAvNavAnsatt != null,
		arkivert = !aktiv || godkjentAvNavAnsatt != null,
		opprettetAvArrangorAnsatt = opprettetAv,
		opprettetDato = createdAt
	)
}
