package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class DeltakerUpsert(
	val id: UUID,
	val gjennomforingId: UUID,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime,
	val dagerPerUke: Int? = null,
	val prosentStilling: Float? = null,
	val begrunnelseForDeltakelse: String?
) {
	fun compareTo(deltaker: Deltaker) = this == DeltakerUpsert(
			id = deltaker.id,
			gjennomforingId = deltaker.gjennomforingId,
			startDato = deltaker.startDato,
			sluttDato = deltaker.sluttDato,
			registrertDato = deltaker.registrertDato,
			dagerPerUke = deltaker.dagerPerUke,
			prosentStilling = deltaker.prosentStilling,
			begrunnelseForDeltakelse = deltaker.begrunnelseForDeltakelse
		)

}
