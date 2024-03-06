package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

data class DeltakerUpsert(
	val id: UUID,
	val gjennomforingId: UUID,
	val statusInsert: DeltakerStatusInsert,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Float? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String?,
	val innhold: DeltakelsesInnhold?,
	val kilde: Kilde,
	val forsteVedtakFattet: LocalDate?,
	val historikk: List<DeltakerHistorikk>?,
	val sistEndretAv: UUID?,
	val sistEndretAvEnhet: UUID?,
) {
	fun compareTo(deltaker: Deltaker): Boolean {
		// Her kan man ikke enkelt sammenlikne hele objektet fordi sammenlikning av localdatetime må rundes av pga ms presisjon
		val statusEquals = statusInsert.type == deltaker.status.type && statusInsert.aarsak == deltaker.status.aarsak
		val deltakerEquals = id == deltaker.id && startDato == deltaker.startDato && sluttDato == deltaker.sluttDato
			&& registrertDato.truncatedTo(ChronoUnit.MILLIS) == deltaker.registrertDato.truncatedTo(ChronoUnit.MILLIS)
			&& dagerPerUke == deltaker.dagerPerUke && prosentStilling == deltaker.prosentStilling
			&& innsokBegrunnelse == deltaker.innsokBegrunnelse && innhold == deltaker.innhold && kilde == deltaker.kilde
			&& forsteVedtakFattet == deltaker.forsteVedtakFattet && sistEndretAv == deltaker.sistEndretAv
			&& sistEndretAvEnhet == deltaker.sistEndretAvEnhet

		return statusEquals && deltakerEquals
	}

}
