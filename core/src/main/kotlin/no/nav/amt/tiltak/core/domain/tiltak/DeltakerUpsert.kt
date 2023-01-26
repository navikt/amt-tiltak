package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerUpsert(
	val id: UUID,
	val gjennomforingId: UUID,
	val statusInsert: DeltakerStatusInsert,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
	val innsokBegrunnelse: String?
) {
	fun compareTo(deltaker: Deltaker) = this == DeltakerUpsert(
			id = deltaker.id,
			statusInsert = DeltakerStatusInsert(
				id = deltaker.status.id,
				deltakerId = deltaker.id,
				type = deltaker.status.type,
				aarsak = deltaker.status.aarsak,
				gyldigFra = deltaker.status.gyldigFra
			),
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			innsokBegrunnelse = deltaker.innsokBegrunnelse
		)

}
