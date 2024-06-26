package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class GjennomforingInput(
	val id: UUID,
	val tiltakId: UUID,
	val arrangorId: UUID,
	val navn: String,
	val status: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime,
	val opprettetAar: Int,
	val lopenr: Int,
	val erKurs: Boolean
) {
	fun toGjennomforing(tiltak: Tiltak, arrangor: Arrangor): Gjennomforing {
		return Gjennomforing(
			id = this.id,
			tiltak = tiltak,
			arrangor = arrangor,
			navn = this.navn,
			status = Gjennomforing.Status.valueOf(this.status),
			startDato = this.startDato,
			sluttDato = this.sluttDato,
			opprettetAar = this.opprettetAar,
			lopenr = this.lopenr,
			erKurs = this.erKurs
		)
	}
}
