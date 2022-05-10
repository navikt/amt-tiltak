package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.deltaker.dto.EndringsmeldingDto
import no.nav.amt.tiltak.deltaker.dto.toDto
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
) {
	fun toDto(bruker: Bruker, opprettetAv: Ansatt) = EndringsmeldingDto(
		id = id,
		bruker = bruker.toDto(),
		startDato = startDato,
		aktiv = aktiv,
		godkjent = ferdiggjortAvNavAnsattId != null,
		arkivert = !aktiv || ferdiggjortAvNavAnsattId != null,
		opprettetAvArrangorAnsatt = opprettetAv.toDto(),
		opprettetDato = createdAt,
	)
}
