package no.nav.amt.tiltak.tiltak.dto

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDateTime

data class DeltakerStatusDto(
	val status: Deltaker.Status,
	val endretDato: LocalDateTime
)
