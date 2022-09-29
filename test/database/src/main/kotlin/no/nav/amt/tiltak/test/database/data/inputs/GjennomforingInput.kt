package no.nav.amt.tiltak.test.database.data.inputs

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.time.LocalDate
import java.util.*

data class GjennomforingInput(
	val id: UUID,
	val tiltakId: UUID,
	val arrangorId: UUID,
	val navn: String,
	val status: String,
	val startDato: LocalDate,
	val sluttDato: LocalDate,
	val registrertDato: LocalDate,
	val fremmoteDato: LocalDate,
	val navEnhetId: UUID?,
	val opprettetAar: Int,
	val lopenr: Int,
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
			registrertDato = this.registrertDato.atStartOfDay(),
			fremmoteDato = this.fremmoteDato.atStartOfDay(),
			navEnhetId = this.navEnhetId,
			opprettetAar = this.opprettetAar,
			lopenr = this.lopenr
		)
	}
}
