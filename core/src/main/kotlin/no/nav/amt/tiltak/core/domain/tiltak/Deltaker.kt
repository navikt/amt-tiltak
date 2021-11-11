package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate

class Deltaker(
	val fornavn: String,
	val etternavn: String,
	val fodselsdato: String,
	val startdato: LocalDate?,
	val sluttdato: LocalDate?,
	val status: Status?
) {

	enum class Status {
		NY_BRUKER, GJENNOMFORES, AVBRUTT, FULLFORT
	}
}
