package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusInsertDbo(
	val id: UUID?,
	val deltakerId: UUID,
	val status: Deltaker.Status,
	val gyldigFra: LocalDateTime,
	val aktiv: Boolean
) {

	companion object {

		fun fromDeltakerStatuser(statuser: List<DeltakerStatus>, deltakerId: UUID) = statuser.map {
			DeltakerStatusInsertDbo(id = it.id, deltakerId = deltakerId, it.status, it.statusGjelderFra, it.aktiv)
		}

		fun fromDeltaker(deltaker: Deltaker) = fromDeltakerStatuser(deltaker.statuser.statuser, deltaker.id)
	}
}
