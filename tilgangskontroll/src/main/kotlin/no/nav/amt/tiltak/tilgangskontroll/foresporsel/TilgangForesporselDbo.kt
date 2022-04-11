package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import java.time.ZonedDateTime
import java.util.*

enum class Beslutning {
	GODKJENT, AVVIST
}

data class TilgangForesporselDbo(
	val id: UUID,
	val personligIdent: String,
	val fornavn: String,
	val mellomnavn: String?,
	val etternavn: String,
	val gjennomforingId: UUID,
	val beslutningAvNavAnsattId: UUID?,
	val tidspunktBeslutning: ZonedDateTime?,
	val beslutning: Beslutning?,
	val gjennomforingTilgangId: UUID?,
	val createdAt: ZonedDateTime,
)
