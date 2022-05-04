package no.nav.amt.tiltak.core.domain.tilgangskontroll

import java.time.ZonedDateTime
import java.util.*

data class TiltaksansvarligGjennomforingTilgang(
	val id: UUID,
	val navAnsattId: UUID,
	val gjennomforingId: UUID,
	val gyldigTil: ZonedDateTime,
	val createdAt: ZonedDateTime,
)
