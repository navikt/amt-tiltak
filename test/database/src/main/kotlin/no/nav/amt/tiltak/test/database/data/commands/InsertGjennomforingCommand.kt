package no.nav.amt.tiltak.test.database.data.commands

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.time.LocalDate
import java.util.*

data class InsertGjennomforingCommand(
	val id: UUID,
	val tiltak_id: UUID,
	val arrangor_id: UUID,
	val navn: String,
	val status: String,
	val start_dato: LocalDate,
	val slutt_dato: LocalDate,
	val registrert_dato: LocalDate,
	val fremmote_dato: LocalDate,
	val nav_enhet_id: UUID?,
	val opprettet_aar: Int?,
	val lopenr: Int?,
) {
	fun toGjennomforing(tiltak: Tiltak, arrangor: Arrangor): Gjennomforing {
		return Gjennomforing(
			id = this.id,
			tiltak = tiltak,
			arrangor = arrangor,
			navn = this.navn,
			status = Gjennomforing.Status.valueOf(this.status),
			startDato = this.start_dato,
			sluttDato = this.slutt_dato,
			registrertDato = this.registrert_dato.atStartOfDay(),
			fremmoteDato = this.fremmote_dato.atStartOfDay(),
			navEnhetId = this.nav_enhet_id,
			opprettetAar = this.opprettet_aar,
			lopenr = this.lopenr
		)
	}
}
