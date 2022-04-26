package no.nav.amt.tiltak.test.database.data.commands

import java.time.ZonedDateTime
import java.util.*

data class InsertTilgangInvitasjonCommand(
	val id: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val opprettetAvNavAnsattId: UUID,
	val erBrukt: Boolean = false,
	val tidspunktBrukt: ZonedDateTime? = null,
	val tilgangForesporselId: UUID? = null,
)
