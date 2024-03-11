package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Vedtak(
	val id: UUID,
	val deltakerId: UUID,
	val fattet: LocalDateTime?,
	val gyldigTil: LocalDateTime?,
	val deltakerVedVedtak: DeltakerVedVedtak,
	val fattetAvNav: Boolean,
	val opprettet: LocalDateTime,
	val opprettetAv: UUID,
	val opprettetAvEnhet: UUID,
	val sistEndret: LocalDateTime,
	val sistEndretAv: UUID,
	val sistEndretAvEnhet: UUID,
)

data class DeltakerVedVedtak(
    val id: UUID,
    val startdato: LocalDate?,
    val sluttdato: LocalDate?,
    val dagerPerUke: Float?,
    val deltakelsesprosent: Float?,
    val bakgrunnsinformasjon: String?,
    val innhold: List<Innhold>,
    val status: DeltakerStatus,
) {
	data class DeltakerStatus(
		val id: UUID,
		val type: no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus.Type,
		val aarsak: Aarsak?,
		val gyldigFra: LocalDateTime,
		val gyldigTil: LocalDateTime?,
		val opprettet: LocalDateTime,
	) {
		data class Aarsak(
			val type: no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus.Aarsak,
			val beskrivelse: String?,
		)
	}
}
