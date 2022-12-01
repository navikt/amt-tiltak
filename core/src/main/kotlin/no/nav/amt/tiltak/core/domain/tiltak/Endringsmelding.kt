package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class Endringsmelding(
	val id: UUID,
	val deltakerId: UUID,
	val utfortAvNavAnsattId: UUID?,
	val utfortTidspunkt: ZonedDateTime?,
	val opprettetAvArrangorAnsattId: UUID,
	val opprettet: ZonedDateTime,
	val status: Status,
	val innhold: Innhold,
) {
	enum class Status {
		AKTIV, UTDATERT, UTFORT
	}

	sealed class Innhold {
		data class LeggTilOppstartsdatoInnhold(
			val oppstartsdato: LocalDate
		) : Innhold()

		data class EndreOppstartsdatoInnhold(
			val oppstartsdato: LocalDate
		) : Innhold()

		data class ForlengDeltakelseInnhold(
			val sluttdato: LocalDate
		) : Innhold()

		data class AvsluttDeltakelseInnhold(
			val sluttdato: LocalDate,
			val aarsak: DeltakerStatus.Aarsak,
		) : Innhold()

		data class DeltakerIkkeAktuellInnhold(
			val aarsak: DeltakerStatus.Aarsak,
		) : Innhold()

		data class EndreDeltakelseProsentInnhold(
			val deltakelseProsent: Int
		) : Innhold()
	}
}

