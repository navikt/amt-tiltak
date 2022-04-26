package no.nav.amt.tiltak.test.database.data.commands

import java.time.ZonedDateTime
import java.util.*

data class InsertTilgangForesporselCommand(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val mellomnavn: String? = null,
	val etternavn: String,
	val gjennomforingId: UUID,
	val beslutningAvNavAnsattId: UUID? = null,
	val tidspunktBeslutning: ZonedDateTime? = null,
	val beslutning: String? = null,
	val gjennomforingTilgangId: UUID? = null,
)
