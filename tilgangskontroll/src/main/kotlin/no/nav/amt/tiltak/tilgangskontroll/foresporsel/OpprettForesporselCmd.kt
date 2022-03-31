package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import java.util.*

data class OpprettForesporselCmd(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val gjennomforingId: UUID,
)
