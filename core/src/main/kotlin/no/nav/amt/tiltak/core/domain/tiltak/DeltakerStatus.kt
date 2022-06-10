package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime
import java.util.*

data class DeltakerStatus(
	val id: UUID,
	val type: Deltaker.Status,
	val gyldigFra: LocalDateTime,
	val opprettetDato: LocalDateTime,
	val aktiv: Boolean
)

