package no.nav.amt.tiltak.bff.nav_ansatt.dto

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.domain.tiltak.EndringsmeldingStatusAarsak
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

data class EndringsmeldingDto(
	val id: UUID,
	val deltaker: DeltakerDto,
	val innhold: Innhold?,
	val status: Status,
	val opprettetDato: ZonedDateTime,
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
			val aarsak: EndringsmeldingStatusAarsakDto,
		) : Innhold()

		data class DeltakerIkkeAktuellInnhold(
			val aarsak: EndringsmeldingStatusAarsakDto,
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
				dagerPerUke = this.dagerPerUke,
				gyldigFraDato = this.gyldigFraDato
			)
		is Endringsmelding.Innhold.EndreSluttdatoInnhold ->
			EndringsmeldingDto.Innhold.EndreSluttdatoInnhold(sluttdato = this.sluttdato)
	}
}

fun EndringsmeldingStatusAarsak.toDto(): EndringsmeldingStatusAarsakDto {
	return when(this.type) {
		EndringsmeldingStatusAarsak.Type.SYK -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.SYK)
		EndringsmeldingStatusAarsak.Type.FATT_JOBB -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.FATT_JOBB)
		EndringsmeldingStatusAarsak.Type.TRENGER_ANNEN_STOTTE -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.TRENGER_ANNEN_STOTTE)
		EndringsmeldingStatusAarsak.Type.IKKE_MOTT -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.IKKE_MOTT)
		EndringsmeldingStatusAarsak.Type.ANNET -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.ANNET, this.beskrivelse)
		EndringsmeldingStatusAarsak.Type.UTDANNING -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.UTDANNING)
		EndringsmeldingStatusAarsak.Type.OPPFYLLER_IKKE_KRAVENE -> EndringsmeldingStatusAarsakDto(EndringsmeldingStatusAarsakDto.Type.OPPFYLLER_IKKE_KRAVENE, this.beskrivelse)

	}
}

fun Endringsmelding.Status.toDto(): EndringsmeldingDto.Status {
	return when(this) {
		Endringsmelding.Status.AKTIV -> EndringsmeldingDto.Status.AKTIV
		Endringsmelding.Status.TILBAKEKALT -> EndringsmeldingDto.Status.TILBAKEKALT
		Endringsmelding.Status.UTDATERT -> EndringsmeldingDto.Status.UTDATERT
		Endringsmelding.Status.UTFORT -> EndringsmeldingDto.Status.UTFORT
	}
}
