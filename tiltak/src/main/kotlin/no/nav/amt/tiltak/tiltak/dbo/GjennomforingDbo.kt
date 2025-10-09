package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class GjennomforingDbo(
	val id: UUID,
	val arrangorId: UUID,
	val tiltakId: UUID,
	val navn: String,
	val status: Gjennomforing.Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val opprettetAar: Int?,
	val lopenr: Int?,
	val erKurs: Boolean,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime,
) {

	fun toGjennomforing(tiltak: Tiltak, arrangor: Arrangor): Gjennomforing {
		return Gjennomforing(
			id = id,
			tiltak = tiltak,
			arrangor = arrangor,
			navn = navn,
			status = status,
			startDato = startDato,
			sluttDato = sluttDato,
			lopenr = lopenr,
			opprettetAar = opprettetAar,
			erKurs = erKurs
		)
	}

	fun update(other: GjennomforingDbo): UpdateCheck<GjennomforingDbo> {
		if (this != other) {
			val updated = this.copy(
				navn = other.navn,
				arrangorId = other.arrangorId,
				status = other.status,
				startDato = other.startDato,
				sluttDato = other.sluttDato,
				lopenr = other.lopenr,
				opprettetAar = other.opprettetAar,
				erKurs = other.erKurs,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}
}
