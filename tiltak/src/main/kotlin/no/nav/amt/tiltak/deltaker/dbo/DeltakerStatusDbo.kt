package no.nav.amt.tiltak.deltaker.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import java.time.LocalDateTime
import java.util.*

data class DeltakerStatusDbo(
	val id: UUID = UUID.randomUUID(),
	val deltakerId: UUID,
	val status: Deltaker.Status,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean
) {
	fun toDeltakerStatus(): DeltakerStatus = DeltakerStatus(id, status, gyldigFra, opprettetDato, aktiv)
}
