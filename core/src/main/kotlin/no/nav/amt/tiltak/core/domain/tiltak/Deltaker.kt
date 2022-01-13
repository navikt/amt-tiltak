package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Deltaker(
	val id: UUID,
	val fornavn: String,
	val etternavn: String,
	val fodselsnummer: String,
	val startDato: LocalDate?,
	val sluttDato: LocalDate?,
	val status: Status,
	val registrertDato: LocalDateTime
) {
	enum class Status {
		VENTER_PA_OPPSTART, DELTAR, HAR_SLUTTET, IKKE_AKTUELL
	}
}

