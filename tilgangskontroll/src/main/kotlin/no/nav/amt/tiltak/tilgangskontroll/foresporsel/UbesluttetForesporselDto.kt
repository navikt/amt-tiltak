package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import java.time.ZonedDateTime
import java.util.*

data class UbesluttetForesporselDto(
	val id: UUID,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val fodselsnummer: String,
	val opprettetDato: ZonedDateTime,
)
