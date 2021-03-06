package no.nav.amt.tiltak.core.domain.tiltak

import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Gjennomforing(
	val id: UUID,
	val tiltak: Tiltak,
	val arrangor: Arrangor,
	val navn: String,
	val status: Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val fremmoteDato: LocalDateTime?,
	val navEnhetId: UUID?,
	val opprettetAar: Int?,
	val lopenr: Int?,
) {
	enum class Status {
		IKKE_STARTET, GJENNOMFORES, AVSLUTTET
	}
}
