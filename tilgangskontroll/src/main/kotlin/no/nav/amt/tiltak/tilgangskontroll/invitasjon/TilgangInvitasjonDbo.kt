package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import java.time.ZonedDateTime
import java.util.*

data class TilgangInvitasjonDbo(
	val id: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val opprettetAvNavAnsattId: UUID,
	val erBrukt: Boolean,
	val tidspunktBrukt: ZonedDateTime?,
	val tilgangForesporselId: UUID?,
	val createdAt: ZonedDateTime,
)
