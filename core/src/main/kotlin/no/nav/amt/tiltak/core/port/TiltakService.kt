package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import java.util.*

interface TiltakService {

	fun getTiltakById(id: UUID): Tiltak

	fun upsertTiltak(id: UUID, navn: String, kode: String): Tiltak

}
