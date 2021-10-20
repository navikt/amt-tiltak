package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import java.util.*

interface TiltakService {

	fun addTiltak(tiltak: Tiltak): Tiltak

	fun addTiltaksinstans(arenaId: Int, instans: TiltakInstans): TiltakInstans

	fun getTiltakFromArenaId(arenaId: String): Tiltak?
}
