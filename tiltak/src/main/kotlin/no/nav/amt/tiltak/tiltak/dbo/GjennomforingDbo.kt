package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class GjennomforingDbo(
	val id: UUID,
	val arenaId: Int,
	val arrangorId: UUID,
	val tiltakId: UUID,
	val navn: String,
	val status: Gjennomforing.Status?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime?,
	val fremmoteDato: LocalDateTime?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toGjennomforing(tiltak: Tiltak): Gjennomforing {
		return Gjennomforing(
			id = id,
			tiltak = tiltak,
			arrangorId = arrangorId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)
	}

	fun update(other: GjennomforingDbo): UpdateCheck<GjennomforingDbo> {
		if (this != other) {
			val updated = this.copy(
				navn = other.navn,
				status = other.status,
				oppstartDato = other.oppstartDato,
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
