package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingDbo(
	val id: UUID,
	val deltakerId: UUID,
	val utfortAvNavAnsattId: UUID?,
	val utfortTidspunkt: ZonedDateTime?,
	val opprettetAvArrangorAnsattId: UUID,
	val status: Endringsmelding.Status,
	val type: Type,
	val innhold: Innhold?,
	val createdAt: ZonedDateTime,
	val modifiedAt: ZonedDateTime
) {

	enum class Type {
		LEGG_TIL_OPPSTARTSDATO,
		ENDRE_OPPSTARTSDATO,
		ENDRE_DELTAKELSE_PROSENT,
		FORLENG_DELTAKELSE,
		AVSLUTT_DELTAKELSE,
		DELTAKER_IKKE_AKTUELL,
		TILBY_PLASS,
		SETT_PAA_VENTELISTE,
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
			val nyDeltakelseProsent: Int,
			val dagerPerUke: Int?,
			val gyldigFraDato: LocalDate?
		): Innhold()

		data class EndreSluttdatoInnhold(
			val sluttdato: LocalDate
		) : Innhold()
	}
}
