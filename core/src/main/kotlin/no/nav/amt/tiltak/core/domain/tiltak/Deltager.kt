package no.nav.amt.tiltak.core.domain.tiltak

import java.time.LocalDateTime

class Deltager(
	val fornavn: String,
	val etternavn: String,
	val fodselsdato: String,
	val startdato: LocalDateTime,
	val sluttdato: LocalDateTime,
	val status: String // TODO: Convert to enum
	)
