package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import java.time.ZonedDateTime
import java.util.*

data class UbruktInvitasjonDbo(
	val id: UUID,
	val opprettetAvNavIdent: String,
	val opprettetDato: ZonedDateTime,
	val gyldigTilDato: ZonedDateTime,
)
