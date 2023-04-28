package no.nav.amt.tiltak.bff.tiltaksarrangor.dto

import no.nav.amt.tiltak.bff.tiltaksarrangor.type.DeltakerStatusAarsak
import no.nav.amt.tiltak.bff.tiltaksarrangor.type.toDto
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val innhold: Innhold?,
	val type: Type
) {

	enum class Type {
		LEGG_TIL_OPPSTARTSDATO,
		ENDRE_OPPSTARTSDATO,
		FORLENG_DELTAKELSE,
		AVSLUTT_DELTAKELSE,
		DELTAKER_IKKE_AKTUELL,
		ENDRE_DELTAKELSE_PROSENT,
		TILBY_PLASS,
		SETT_PAA_VENTELISTE,
		ENDRE_SLUTTDATO
	}

	sealed class Innhold {
		data class LeggTilOppstartsdatoInnhold(
			val oppstartsdato: LocalDate,
		) : Innhold()

		data class EndreOppstartsdatoInnhold(
			val oppstartsdato: LocalDate,
		) : Innhold()

		data class EndreDeltakelseProsentInnhold(
			val deltakelseProsent: Int,
			val gyldigFraDato: LocalDate?
		) : Innhold()

		data class ForlengDeltakelseInnhold(
			val sluttdato: LocalDate,
		) : Innhold()

		data class AvsluttDeltakelseInnhold(
			val sluttdato: LocalDate,
			val aarsak: DeltakerStatusAarsak,
		) : Innhold()

		data class DeltakerIkkeAktuellInnhold(
			val aarsak: DeltakerStatusAarsak,
		) : Innhold()

		data class EndreSluttdatoInnhold(
			val sluttdato: LocalDate
		) : Innhold()
	}

}

fun Endringsmelding.Innhold.toDto(): EndringsmeldingDto.Innhold {
	return when(this) {
		is Endringsmelding.Innhold.LeggTilOppstartsdatoInnhold ->
			EndringsmeldingDto.Innhold.LeggTilOppstartsdatoInnhold(this.oppstartsdato)
		is Endringsmelding.Innhold.EndreOppstartsdatoInnhold ->
			EndringsmeldingDto.Innhold.EndreOppstartsdatoInnhold(this.oppstartsdato)
		is Endringsmelding.Innhold.ForlengDeltakelseInnhold ->
			EndringsmeldingDto.Innhold.ForlengDeltakelseInnhold(this.sluttdato)
		is Endringsmelding.Innhold.AvsluttDeltakelseInnhold ->
			EndringsmeldingDto.Innhold.AvsluttDeltakelseInnhold(this.sluttdato, this.aarsak.toDto())
		is Endringsmelding.Innhold.DeltakerIkkeAktuellInnhold ->
			EndringsmeldingDto.Innhold.DeltakerIkkeAktuellInnhold(this.aarsak.toDto())
		is Endringsmelding.Innhold.EndreDeltakelseProsentInnhold ->
			EndringsmeldingDto.Innhold.EndreDeltakelseProsentInnhold(
				deltakelseProsent = this.deltakelseProsent,
				gyldigFraDato = this.gyldigFraDato
			)
		is Endringsmelding.Innhold.EndreSluttdatoInnhold -> EndringsmeldingDto.Innhold.EndreSluttdatoInnhold(this.sluttdato)
	}
}

fun Endringsmelding.Type.toDto(): EndringsmeldingDto.Type {
	return when (this) {
		Endringsmelding.Type.LEGG_TIL_OPPSTARTSDATO -> EndringsmeldingDto.Type.LEGG_TIL_OPPSTARTSDATO
		Endringsmelding.Type.ENDRE_OPPSTARTSDATO -> EndringsmeldingDto.Type.ENDRE_OPPSTARTSDATO
		Endringsmelding.Type.FORLENG_DELTAKELSE -> EndringsmeldingDto.Type.FORLENG_DELTAKELSE
		Endringsmelding.Type.AVSLUTT_DELTAKELSE -> EndringsmeldingDto.Type.AVSLUTT_DELTAKELSE
		Endringsmelding.Type.DELTAKER_IKKE_AKTUELL -> EndringsmeldingDto.Type.DELTAKER_IKKE_AKTUELL
		Endringsmelding.Type.ENDRE_DELTAKELSE_PROSENT -> EndringsmeldingDto.Type.ENDRE_DELTAKELSE_PROSENT
		Endringsmelding.Type.TILBY_PLASS -> EndringsmeldingDto.Type.TILBY_PLASS
		Endringsmelding.Type.SETT_PAA_VENTELISTE -> EndringsmeldingDto.Type.SETT_PAA_VENTELISTE
		Endringsmelding.Type.ENDRE_SLUTTDATO -> EndringsmeldingDto.Type.ENDRE_SLUTTDATO
	}
}

fun Endringsmelding.toDto() = EndringsmeldingDto(id = id, innhold = innhold?.toDto(), type=type.toDto())
