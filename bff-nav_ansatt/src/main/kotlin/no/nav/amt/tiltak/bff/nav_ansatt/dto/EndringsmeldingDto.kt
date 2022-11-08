package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.bff.tiltaksarrangor.type.DeltakerStatusAarsak
import no.nav.amt.tiltak.bff.tiltaksarrangor.type.toDto
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val deltaker: DeltakerDto,
	val innhold: Innhold,
	val status: Status,
	val opprettetDato: ZonedDateTime,
) {
	val type = innhold.type()

	enum class Type {
		LEGG_TIL_OPPSTARTSDATO,
		ENDRE_OPPSTARTSDATO,
		FORLENG_DELTAKELSE,
		AVSLUTT_DELTAKELSE,
		DELTAKER_IKKE_AKTUELL,
	}

	enum class Status {
		AKTIV, UTDATERT, UTFORT
	}

	sealed class Innhold {
		data class LeggTilOppstartsdatoInnhold(
			val oppstartsdato: LocalDate,
		) : Innhold()

		data class EndreOppstartsdatoInnhold(
			val oppstartsdato: LocalDate,
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

		fun type(): Type {
			return when(this) {
				is LeggTilOppstartsdatoInnhold -> Type.LEGG_TIL_OPPSTARTSDATO
				is EndreOppstartsdatoInnhold -> Type.ENDRE_OPPSTARTSDATO
				is ForlengDeltakelseInnhold -> Type.FORLENG_DELTAKELSE
				is AvsluttDeltakelseInnhold -> Type.AVSLUTT_DELTAKELSE
				is DeltakerIkkeAktuellInnhold -> Type.DELTAKER_IKKE_AKTUELL
			}
		}
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
	}
}

fun Endringsmelding.Status.toDto(): EndringsmeldingDto.Status {
	return when(this) {
		Endringsmelding.Status.AKTIV -> EndringsmeldingDto.Status.AKTIV
		Endringsmelding.Status.UTDATERT -> EndringsmeldingDto.Status.UTDATERT
		Endringsmelding.Status.UTFORT -> EndringsmeldingDto.Status.UTFORT
	}
}
