package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatuser
import java.time.LocalDate
import java.util.*

data class DeltakerStatusDbo(
	val id: UUID = UUID.randomUUID(),
	val deltakerId: UUID,
	val status: Deltaker.Status,
	val endretDato: LocalDate,
	val aktiv: Boolean
) {

	fun toDeltakerStatus(): DeltakerStatus = DeltakerStatus(id, status, endretDato, aktiv)

	companion object {

		fun fromDeltakerStatuser(statuser: List<DeltakerStatus>, deltakerId: UUID) = statuser.map {
			DeltakerStatusDbo(it.id, deltakerId, it.status, it.endretDato, it.aktiv)
		}

		fun fromDeltaker(deltaker: Deltaker) = fromDeltakerStatuser(deltaker.statuser.statuser, deltaker.id)
	}
}

fun List<DeltakerStatusDbo>.toDeltakerStatuser() = DeltakerStatuser(map { it.toDeltakerStatus() } )
