package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
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
	val innhold: Innhold,
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
	}

	data class DeltakerStatusAarsak(
		val type: DeltakerStatus.Aarsak.Type,
		val beskrivelse: String? = null,
	)

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
			val aarsak: DeltakerStatusAarsak,
		) : Innhold()

		data class DeltakerIkkeAktuellInnhold(
			val aarsak: DeltakerStatusAarsak,
		) : Innhold()

		data class EndreDeltakelseProsentInnhold(
			val nyDeltakelseProsent: Int
		): Innhold()

		fun type(): Type {
			return when(this) {
				is LeggTilOppstartsdatoInnhold -> Type.LEGG_TIL_OPPSTARTSDATO
				is EndreOppstartsdatoInnhold -> Type.ENDRE_OPPSTARTSDATO
				is ForlengDeltakelseInnhold -> Type.FORLENG_DELTAKELSE
				is AvsluttDeltakelseInnhold -> Type.AVSLUTT_DELTAKELSE
				is DeltakerIkkeAktuellInnhold -> Type.DELTAKER_IKKE_AKTUELL
				is EndreDeltakelseProsentInnhold -> Type.ENDRE_DELTAKELSE_PROSENT
			}
		}
	}
}
