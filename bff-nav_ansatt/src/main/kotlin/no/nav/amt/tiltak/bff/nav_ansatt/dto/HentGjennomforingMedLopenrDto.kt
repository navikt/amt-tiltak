package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.time.LocalDate
import java.util.*

data class HentGjennomforingMedLopenrDto(
	val id: UUID,
	val navn: String,
	val lopenr: Int,
	val status: Status,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val opprettetAr: Int,
	val arrangorNavn: String,
	val tiltak: TiltakDto,
) {

	enum class Status {
		IKKE_STARTET, GJENNOMFORES, AVSLUTTET
	}

}

fun Gjennomforing.Status.toDto(): HentGjennomforingMedLopenrDto.Status {
	return when(this) {
		Gjennomforing.Status.GJENNOMFORES -> HentGjennomforingMedLopenrDto.Status.GJENNOMFORES
		Gjennomforing.Status.AVSLUTTET -> HentGjennomforingMedLopenrDto.Status.AVSLUTTET
		Gjennomforing.Status.IKKE_STARTET -> HentGjennomforingMedLopenrDto.Status.IKKE_STARTET
	}
}
