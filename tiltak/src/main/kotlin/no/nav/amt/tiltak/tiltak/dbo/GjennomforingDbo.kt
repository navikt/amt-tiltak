package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class GjennomforingDbo(
	val id: UUID,
	val arrangorId: UUID,
	val tiltakId: UUID,
	val navn: String,
	val status: Gjennomforing.Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val navEnhetId: UUID?,
	val opprettetAar: Int,
	val lopenr: Int,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime,
	val deprecated: Boolean = true,
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
			navEnhetId = navEnhetId,
			lopenr = lopenr,
			opprettetAar = opprettetAar,
		)
	}

	fun update(other: GjennomforingDbo): UpdateCheck<GjennomforingDbo> {
		if (this != other) {
			val updated = this.copy(
				navn = other.navn,
				status = other.status,
				startDato = other.startDato,
				sluttDato = other.sluttDato,
				navEnhetId = other.navEnhetId,
				lopenr = other.lopenr,
				opprettetAar = other.opprettetAar,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}
}
