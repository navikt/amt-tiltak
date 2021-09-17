package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime

class Deltager(
	val fornavn: String,
	val etternavn: String,
	val fodselsdato: String,
	val startdato: LocalDateTime,
	val sluttdato: LocalDateTime,
	val status: Status // TODO: Convert to enum
	) {

	enum class Status {
		NY_BRUKER, GJENNOMFORES, AVBRUTT, FULLFORT
	}
}
