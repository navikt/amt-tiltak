package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltakInstans(
	val id: UUID,
	val tiltakId: UUID,
	val tiltaksarrangorId: UUID,
	val navn: String,
	val status: Status?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime?,
	val fremmoteDato: LocalDateTime?,
) {
	enum class Status {
		GJENNOMFORES, AVSLUTTET, IKKE_STARTET
	}
}
