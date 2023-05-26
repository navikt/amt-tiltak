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
	val innhold: Innhold?,
	val type: Type
) {
	enum class Status {
		AKTIV, TILBAKEKALT, UTDATERT, UTFORT
	}

	enum class Type {
		LEGG_TIL_OPPSTARTSDATO,
		ENDRE_OPPSTARTSDATO,
		FORLENG_DELTAKELSE,
		AVSLUTT_DELTAKELSE,
		DELTAKER_IKKE_AKTUELL,
		ENDRE_DELTAKELSE_PROSENT,
		DELTAKER_ER_AKTUELL,
		ENDRE_SLUTTDATO
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
			val aarsak: EndringsmeldingStatusAarsak,
		) : Innhold()

		data class DeltakerIkkeAktuellInnhold(
			val aarsak: EndringsmeldingStatusAarsak,
		) : Innhold()

		data class EndreDeltakelseProsentInnhold(
			val deltakelseProsent: Int,
			val dagerPerUke: Int?,
			val gyldigFraDato: LocalDate?
		) : Innhold()

		data class EndreSluttdatoInnhold(
			val sluttdato: LocalDate
		) : Innhold()
	}
}
