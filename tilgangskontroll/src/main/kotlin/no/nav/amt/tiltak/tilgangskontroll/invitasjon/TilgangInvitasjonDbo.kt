package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import java.time.ZonedDateTime
import java.util.*

data class TilgangInvitasjonDbo(
	val id: UUID,
	val gjennomforingId: UUID,
	val gydligTil: ZonedDateTime,
	val opprettetAvNavAnsattId: UUID,
	val erBrukt: UUID,
	val tidspunkt_brukt: UUID,
	val tilgangForesporselId: UUID,
	val createdAt: ZonedDateTime,
)
