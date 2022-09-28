package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig

import java.time.ZonedDateTime
import java.util.*

data class TiltaksansvarligGjennomforingTilgangDbo(
	val id: UUID,
	val navAnsattId: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
