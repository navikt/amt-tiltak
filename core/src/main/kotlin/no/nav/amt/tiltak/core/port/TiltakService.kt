package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.util.*

interface TiltakService {

	fun getTiltakById(id: UUID): Tiltak

	fun getTiltakFromArenaId(arenaId: String): Tiltak?

	fun upsertTiltak(arenaId: String, navn: String, kode: String): Tiltak

	fun upsertTiltak(id: UUID, navn: String, kode: String): Tiltak

}
