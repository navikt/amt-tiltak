package no.nav.amt.tiltak.bff.tiltaksarrangor.request

import java.time.LocalDate

data class AvsluttDeltakelseRequest (
	val sluttdato: LocalDate,
	val aarsak: EndringsmeldingStatusAarsakDto,
)
