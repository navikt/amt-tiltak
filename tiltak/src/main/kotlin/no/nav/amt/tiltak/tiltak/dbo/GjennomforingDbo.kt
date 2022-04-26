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
	val registrertDato: LocalDateTime,
	val fremmoteDato: LocalDateTime?,
	val navEnhetId: UUID?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
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
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)
	}

	fun update(other: GjennomforingDbo): UpdateCheck<GjennomforingDbo> {
		if (this != other) {
			val updated = this.copy(
				navn = other.navn,
				status = other.status,
				startDato = other.startDato,
				sluttDato = other.sluttDato,
				registrertDato = other.registrertDato,
				fremmoteDato = other.fremmoteDato,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}
}
