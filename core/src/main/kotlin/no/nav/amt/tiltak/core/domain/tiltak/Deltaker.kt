package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDate
import java.util.*

class Deltaker(
	val id: UUID,
	val fornavn: String,
	val etternavn: String,
	val fodselsdato: Fodselsnummer,
	val startdato: LocalDate?,
	val sluttdato: LocalDate?,
	val status: Status?
) {

	enum class Status {
		NY_BRUKER, GJENNOMFORES, AVBRUTT, FULLFORT
	}
}
