package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import no.nav.amt.tiltak.bff.tiltaksarrangor.type.DeltakerStatusAarsak
import java.time.LocalDate

data class AvsluttDeltakelseRequest (
	val sluttdato: LocalDate,
	val aarsak: DeltakerStatusAarsak,
)
