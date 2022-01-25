package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusDbo(
	val id: UUID = UUID.randomUUID(),
	val deltakerId: UUID,
	val status: Deltaker.Status,
	val endretDato: LocalDateTime,
	val aktiv: Boolean
) {

	fun toDeltakerStatus(): DeltakerStatus = DeltakerStatus(id, status, endretDato, aktiv)

	companion object {

		fun fromDeltaker(deltaker: Deltaker) = deltaker.statuser.statuser.map {
			DeltakerStatusDbo(it.id, deltaker.id, it.status, it.endretDato, it.aktiv)
		}
	}
}

fun List<DeltakerStatusDbo>.toDeltakerStatuser() = DeltakerStatuser(map { it.toDeltakerStatus() } )
