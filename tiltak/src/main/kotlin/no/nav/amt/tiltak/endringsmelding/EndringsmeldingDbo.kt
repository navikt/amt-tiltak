package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val ferdiggjortAvNavAnsattId: UUID?,
	val ferdiggjortTidspunkt: ZonedDateTime?,
	val aktiv: Boolean,
	val opprettetAvArrangorAnsattId: UUID,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toModel(): Endringsmelding {
		return Endringsmelding(
			id = id,
			deltakerId = deltakerId,
			startDato = startDato,
			sluttDato = sluttDato,
			ferdiggjortAvNavAnsattId = ferdiggjortAvNavAnsattId,
			ferdiggjortTidspunkt = ferdiggjortTidspunkt,
			aktiv = aktiv,
			opprettetAvArrangorAnsattId = opprettetAvArrangorAnsattId,
			opprettet = createdAt
		)
	}

}
