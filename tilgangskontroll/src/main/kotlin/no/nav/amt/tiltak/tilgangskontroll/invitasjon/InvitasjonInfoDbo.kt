package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import java.time.ZonedDateTime

data class InvitasjonInfoDbo(
	val overordnetEnhetNavn: String,
	val gjennomforingNavn: String,
	val erBrukt: Boolean,
	val gyldigTil: ZonedDateTime
)
